package com.lazycece.grpc.demo.service.unary;

import com.lazycece.grpc.demo.proto.unary.HelloRequest;
import com.lazycece.grpc.demo.proto.unary.HelloResponse;
import com.lazycece.grpc.demo.proto.unary.UnaryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/9/11
 */
public class UnaryClient {

    private static final Logger logger = Logger.getLogger(UnaryClient.class.getName());

    private final ManagedChannel channel;
    private final UnaryServiceGrpc.UnaryServiceBlockingStub blockingStub;


    public UnaryClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    private UnaryClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = UnaryServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void sayHello(String name) {
        logger.info("ready ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info(response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        UnaryClient client = new UnaryClient("localhost", 50051);
        try {
            client.sayHello("lazycece");
        } finally {
            client.shutdown();
        }
    }
}
