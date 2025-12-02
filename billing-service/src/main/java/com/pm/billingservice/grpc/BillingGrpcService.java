package com.pm.billingservice.grpc;


import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//////BillingServiceImplBase
@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest,
                                     StreamObserver<billing.BillingResponse> responseObserver){
        logger.info("createBillingAccount request received {} ", billingRequest.toString());

        // Business logic -

        billing.BillingResponse response = billing.BillingResponse.newBuilder()
                .setAccountId(billingRequest.getPatientId())
                .setStatus("ACTIVE")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }


}
