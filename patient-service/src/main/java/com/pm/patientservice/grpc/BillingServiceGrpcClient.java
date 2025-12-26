package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.patientservice.kafka.KafkaProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class BillingServiceGrpcClient {
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
    Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final KafkaProducer kafkaProducer;
//    In local dev-environment url : localhost:9001/BillingService/CreatePatientAccount
//    In aws installation url : aws.grpc:12234/BillingService/CreatePatientAccount
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort,
            KafkaProducer kafkaProducer
    ) {
        log.info("Connecting to Billing Service GRPC service at {}:{}", serverAddress, serverPort);


        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();

        blockingStub = BillingServiceGrpc.newBlockingStub(channel);

        this.kafkaProducer = kafkaProducer;

    }

    @CircuitBreaker(name="billingService", fallbackMethod = "billingFallback")   //----------This is the way to apply having name and fallback method which is used to determine what method to use if circuit is open or GRPS req is continiously failing
    @Retry(name = "billingRetry" ) //---------------- Specifies we want to attempt the request 3 times before it opens the circuit and if - then fall back method is applied
    public BillingResponse createBillingAccount(String patientId, String name, String email){

        BillingRequest request = BillingRequest.newBuilder().setPatientId(patientId).setName(name).setEmail(email).build();

        BillingResponse response = blockingStub.createBillingAccount(request);

        log.info("Received response from billing service via GRPC: {}", response);

        return response;
    }

    //-------------This is a fall back method which should be designed specifically with the method signature same as the method on which circuit breaker is applied.
    //-------------here Throwable t is the exception which is passed by resilience4j package after circuit breaker is applied from the infected method
    public BillingResponse billingFallback(String patientId, String name, String email, Throwable t){
        log.warn("[CIRCUIT BREAKER]: Billing service is unavailable. Triggered " + "fallback: {}", t.getMessage());

        kafkaProducer.sendBillingAccountEvent(patientId, name, email);

        return BillingResponse.newBuilder()
                .setAccountId("")
                .setStatus("PENDING")
                .build();

    }
}
