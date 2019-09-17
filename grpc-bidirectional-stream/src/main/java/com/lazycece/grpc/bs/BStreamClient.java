package com.lazycece.grpc.bs;

import com.lazycece.grpc.bs.proto.BStreamServiceGrpc;
import com.lazycece.grpc.bs.proto.HelloRequest;
import com.lazycece.grpc.bs.proto.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/9/11
 */
public class BStreamClient {
    private static final Logger logger = Logger.getLogger(BStreamClient.class.getName());
    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);
    private final BStreamServiceGrpc.BStreamServiceStub stub;
    private final ManagedChannel channel;

    public BStreamClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    private BStreamClient(ManagedChannel channel) {
        this.channel = channel;
        stub = BStreamServiceGrpc.newStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void sayHello(String name) {
        logger.info("ready ...");
        StreamObserver<HelloRequest> requestStreamObserver = stub.sayHello(new CustomClientResponseObserver());
        logger.info("finish ...");
    }

    public static void main(String[] args) throws InterruptedException {
        BStreamClient client = new BStreamClient("localhost", 50051);
        client.sayHello("lazycece");
        COUNT_DOWN_LATCH.await(1, TimeUnit.MINUTES);
        client.shutdown();
    }

    static class CustomClientResponseObserver implements ClientResponseObserver<HelloRequest, HelloResponse> {

        ClientCallStreamObserver<HelloRequest> requestStream;

        @Override
        public void beforeStart(ClientCallStreamObserver<HelloRequest> requestStream) {
            this.requestStream = requestStream;
            requestStream.disableAutoInboundFlowControl();
            requestStream.setOnReadyHandler(new Runnable() {
                List<String> names = Arrays.asList("lazycece", "grpc");
                Iterator<String> iterator = names.iterator();

                @Override
                public void run() {
                    while (requestStream.isReady()) {
                        if (iterator.hasNext()) {
                            String name = iterator.next();
                            requestStream.onNext(HelloRequest.newBuilder().setName(name).build());
                        } else {
                            requestStream.onCompleted();
                        }
                    }
                }
            });
        }

        @Override
        public void onNext(HelloResponse value) {
            logger.info(value.getMessage());
            // Signal the sender to send one message.
            requestStream.request(1);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
            COUNT_DOWN_LATCH.countDown();
        }

        @Override
        public void onCompleted() {
            logger.info("on completed");
            COUNT_DOWN_LATCH.countDown();
        }
    }
}
