package com.pm.stack;


import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.elasticache.CfnCacheCluster;
import software.amazon.awscdk.services.elasticache.CfnSubnetGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack{

    private final Vpc vpc;

    private final Cluster ecsCluster;

    private final CfnCacheCluster elasticCacheCluster;


    public LocalStack(final App scope, final String id, final StackProps props){
        super(scope, id, props);

        this.vpc = createVpc();

//        Database creation

        DatabaseInstance authServiceDb =
                createDatabaseInstance("AuthServiceDB" ,"auth-service-db" );

        DatabaseInstance patientServiceDb =
                createDatabaseInstance("PatientServiceDB" ,"patient-service-db" );

//        Health check creation

        CfnHealthCheck authDbHealthCheck =
                createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");

        CfnHealthCheck patientDbHealthCheck =
                createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");

//         Kafka creation

        CfnCluster mskCluster = createMskCluster();

//        ECS cluster creation

        this.ecsCluster = createEcsCluster();
        this.elasticCacheCluster = createRedisCluster();

//        Fargate Micro-service creation - auth-service and auth-service-db with auth-service-db to start first and then auth-service.

        FargateService authService =
                createFargateService("AuthService",
                        "auth-service",
                        List.of(4005),
                        authServiceDb,
                        Map.of("JWT_SECRET", "f+vZr9J3jYMi/C88KnaHC6lGbkjJuJ8KNY1fP71jPtU="));

        authService.getNode().addDependency(authDbHealthCheck);
        authService.getNode().addDependency(authServiceDb);

//        Fargate service - Billing Microservice creation
        FargateService billingService =
                createFargateService("BillingService",
                        "billing-service",
                        List.of(4001,9001),
                        null,
                        null);

//
        FargateService analyticsService =
                createFargateService("AnalyticsService",
                        "analytics-service",
                        List.of(4002),
                        null,
                        null);
        analyticsService.getNode().addDependency(mskCluster);

//
        FargateService patientService =
                createFargateService("PatientService",
                        "patient-service", //Image name and container/service name are same for better mapping of dns-names for cloud service discovery.
                        List.of(4000),
                        patientServiceDb,
                        Map.of(
                                "BILLING_SERVICE_ADDRESS","billing-service.patient-management.local",   //This is useful in development - 'host.docker.internal' as internal docker network
//                                requires the service address convention for dev environment like this. But now for production grade we need to change
                                "BILLING_SERVICE_GRPC_PORT","9001"
                        ));

        patientService.getNode().addDependency(patientDbHealthCheck);
        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(billingService);

        patientService.getNode().addDependency(mskCluster);
        patientService.getNode().addDependency(elasticCacheCluster);


//        APIGateway Microservice creation

        ApplicationLoadBalancedFargateService apiGateway =
                createApiGatewayService();

        apiGateway.getNode().addDependency(elasticCacheCluster);

        //Creating fargate service and specifying the prometheus-prod image to be taken to create task definition
        FargateService prometheusService = createFargateService(
            "PrometheusService",
                "prometheus-prod",
                List.of(9090),
                null,
                null
        );

//
        createGrafanaService();



    }

    private Vpc createVpc(){
        return Vpc.Builder.
                create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabaseInstance(String id, String dbName){
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()
                ) )
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db , String id){
        return CfnHealthCheck.Builder.create(this,id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("3.6.0")
                .numberOfBrokerNodes(4)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder() // This connects all the broker nodes with each other
                        .instanceType("kafka.m5.xlarge")
                .clientSubnets(vpc.getPrivateSubnets().stream()
                .map(ISubnet::getSubnetId)
                .collect(Collectors.toList()))
                .brokerAzDistribution("DEFAULT").build())
        .build();
    }

//    CLuster creation function with Service discovery in such a way that no ip address identification is needed and entire mapping
//    will be done in terms of naming conventions just like in Docker for example -
//    Address for Auth-service for other internal services will be - auth-service.patient-management.local
//    For ex- Internally inside docker network billing-service will have a domain name like - billing-service.patient-management.local
//    What we are trying to establish in this branch is every other service should be able to register themselves with the default CloudNameSpace declared here
//    so that the naming convention service dsicovery feature can be enabled in aws cloud as well
    private Cluster createEcsCluster(){
        return Cluster.Builder.create(this,"PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local")
                        .build())
                .build();
    }

    private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance db, Map<String, String> additionalEnvVars) {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions.Builder containerOptions =
            ContainerDefinitionOptions.builder()
                    .image(ContainerImage.fromRegistry(imageName))
                    .portMappings(ports.stream()
                            .map(port -> PortMapping.builder()
                                    .containerPort(port)
                                    .hostPort(port)
                                    .protocol(Protocol.TCP)
                                    .build())
                            .toList())
                    .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                    .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                            .logGroupName("/ecs/" + imageName)
                                            .removalPolicy(RemovalPolicy.DESTROY)
                                            .retention(RetentionDays.ONE_DAY)
                                            .build())
                                    .streamPrefix(imageName)
                            .build()));

        Map<String, String> envVars = new HashMap<>();

//        Enable any service using this method to link to kafka - brokers if required on these three ports in future
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511,localhost.localstack.cloud:4512");

//        Enable fargate service to find where the ecs redis cluster host and port is
        envVars.put("SPRING_CACHE_TYPE", "redis");
        envVars.put("SPRING_DATA_REDIS_HOST", elasticCacheCluster.getAttrRedisEndpointAddress());
        envVars.put("SPRING_DATA_REDIS_PORT", elasticCacheCluster.getAttrRedisEndpointPort());

        if(additionalEnvVars != null){
            envVars.putAll(additionalEnvVars);
        }

        if(db != null){
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
//            For development purpose
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");


            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        // Converted the containerOptions object to be of the builder instance not the instance of ContainerDefinitionOptions's alone because - we need environment variables to be added before building the container options.
        // We did this because we want to add environment variables later so to add it after applying the above logics.
        containerOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .cloudMapOptions(CloudMapOptions.builder()
                        .name(imageName)
                        .dnsRecordType(DnsRecordType.A)
                        .build())
                .serviceName(imageName)
                .build();
    }
//      It takes spring cloud gateway , spring boot app and it creates application load balancer and fargate service in order to expose the services in ecs cluster to the world
    private ApplicationLoadBalancedFargateService createApiGatewayService(){
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "APIGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("api-gateway"))
                        .environment(Map.of(
                                "SPRING_PROFILES_ACTIVE", "prod",
                                "AUTH_SERVICE_URL","http://auth-service.patient-management.local:4005",  // This url will be changed when shifting from dev region(http://host.docker.internal:4005) to production region
                                    "REDIS_HOST", elasticCacheCluster.getAttrRedisEndpointAddress(),
                                "REDIS_PORT",elasticCacheCluster.getAttrRedisEndpointPort()
                        ))
                        .portMappings(List.of(4004).stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                        .logGroupName("/ecs/api-gateway")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix("api-gateway")
                                .build()))
                        .build();

        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService apiGateway
                = ApplicationLoadBalancedFargateService.Builder
                .create(this,"APIGatewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .publicLoadBalancer(true)           //This will enable and attach public Load Balancer which is public to the world and add's up as service to "CloudMap Discovery service" and attached to api-gateway.
                .cloudMapOptions(CloudMapOptions.builder()
                        .name("api-gateway")
                        .dnsRecordType(DnsRecordType.A)  //This line will basically map let's say billing-service.patient-management.local to actual ip address assigned in ecs cluster
                        .build())
                .build();

        return apiGateway;

    }

    //    Code for creating elastic cache cluster for local stack mimicking aws
    private CfnCacheCluster createRedisCluster(){
        CfnSubnetGroup redisSubnetGroup = CfnSubnetGroup.Builder
                .create(this, "RedisSubnetGroup")
                .description("Redis/elasticcache subnet group")
                .subnetIds(vpc.getPrivateSubnets().stream()
                        .map(ISubnet::getSubnetId)
                        .collect(Collectors.toList()))
                .build();

        return CfnCacheCluster.Builder.create(this,"RedisCluster")
                .cacheNodeType("cache.t2.micro")
                .engine("redis")
                .numCacheNodes(1)
                .cacheSubnetGroupName(redisSubnetGroup.getCacheSubnetGroupName())
                .vpcSecurityGroupIds(List.of(vpc.getVpcDefaultSecurityGroup()))
                .build();
    }

    private void createGrafanaService(){
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this,"GrafanaService")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        taskDefinition.addContainer("GrafanaContainer", ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("grafana/grafana"))
                        .portMappings(List.of(PortMapping.builder()
                                        .containerPort(3000) //This is for opening the port for outside ECS cluster
                                .build()))
                .build());

        ApplicationLoadBalancedFargateService service = ApplicationLoadBalancedFargateService.Builder
                .create(this,"GrafanaUIService")
                .taskDefinition(taskDefinition)
                .publicLoadBalancer(true)
                .listenerPort(3000)
                .desiredCount(1)
                .build();

    }


    public static void main(final String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();
        new LocalStack(app, "localstack", props);
        app.synth();
        System.out.println("App synthesizing is in progress....");
    }

}
