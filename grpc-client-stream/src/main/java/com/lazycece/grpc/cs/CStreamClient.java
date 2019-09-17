package com.lazycece.grpc.cs;

import com.lazycece.grpc.cs.proto.CStreamServiceGrpc;
import com.lazycece.grpc.cs.proto.HelloRequest;
import com.lazycece.grpc.cs.proto.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/9/11
 */
public class CStreamClient {

    private static final Logger logger = Logger.getLogger(CStreamClient.class.getName());
    private final ManagedChannel channel;
    private final CStreamServiceGrpc.CStreamServiceStub stub;
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    private CStreamClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    private CStreamClient(ManagedChannel channel) {
        this.channel = channel;
        stub = CStreamServiceGrpc.newStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void sayHello(String name) {
        logger.info("ready ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        StreamObserver<HelloRequest> requestStreamObserver = stub.sayHello(new CustomStreamObserver());
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();
        logger.info("finish ...");
    }

    public static void main(String[] args) throws InterruptedException {
        CStreamClient client = new CStreamClient("localhost", 50051);
        client.sayHello("lazycece");
        COUNT_DOWN_LATCH.await(1, TimeUnit.MINUTES);
        client.shutdown();
    }

    class CustomStreamObserver implements StreamObserver<HelloResponse> {

        @Override
        public void onNext(HelloResponse helloResponse) {
            logger.info(helloResponse.getMessage());
        }

        @Override
        public void onError(Throwable throwable) {
            logger.log(Level.WARNING, throwable.getMessage());
            COUNT_DOWN_LATCH.countDown();
        }

        @Override
        public void onCompleted() {
            logger.info("on completed");
            COUNT_DOWN_LATCH.countDown();
        }
    }

}
