package com.lazycece.grpc.ss;

import com.lazycece.grpc.ss.proto.HelloRequest;
import com.lazycece.grpc.ss.proto.HelloResponse;
import com.lazycece.grpc.ss.proto.SStreamServiceGrpc;
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
public class SStreamClient {

    private static final Logger logger = Logger.getLogger(SStreamClient.class.getName());

    private final ManagedChannel channel;
    private final SStreamServiceGrpc.SStreamServiceStub stub;
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    public SStreamClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    private SStreamClient(ManagedChannel channel) {
        this.channel = channel;
        stub = SStreamServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sayHello(String name) {
        logger.info("ready ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        CustomStreamObserver streamObserver = new CustomStreamObserver();
        stub.sayHello(request, streamObserver);
    }

    public static void main(String[] args) throws InterruptedException {
        SStreamClient sStreamClient = new SStreamClient("localhost", 50051);
        sStreamClient.sayHello("lazycece");
        SStreamClient.COUNT_DOWN_LATCH.await();
        sStreamClient.shutdown();
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
