package com.lazycece.grpc.cs;

import com.lazycece.grpc.cs.proto.CStreamServiceGrpc;
import com.lazycece.grpc.cs.proto.HelloRequest;
import com.lazycece.grpc.cs.proto.HelloResponse;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/09/16
 */
public class CStreamServiceImpl extends CStreamServiceGrpc.CStreamServiceImplBase {

    private static final Logger logger = Logger.getLogger(CStreamServiceImpl.class.getName());

    @Override
    public StreamObserver<HelloRequest> sayHello(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest helloRequest) {
                HelloResponse response = HelloResponse.newBuilder().setMessage("hello, " + helloRequest.getName()).build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warning("on error: " + throwable.getMessage());
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                logger.info("on completed .");
                responseObserver.onCompleted();
            }
        };
    }
}