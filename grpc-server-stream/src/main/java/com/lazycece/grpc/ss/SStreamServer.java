package com.lazycece.grpc.ss;

import com.lazycece.grpc.ss.proto.HelloRequest;
import com.lazycece.grpc.ss.proto.HelloResponse;
import com.lazycece.grpc.ss.proto.SStreamServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/9/11
 */
public class SStreamServer {

    private static final Logger logger = Logger.getLogger(SStreamServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new SStreamServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SStreamServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final SStreamServer sStreamServer = new SStreamServer();
        sStreamServer.start();
        sStreamServer.blockUntilShutdown();
    }

    static class SStreamServiceImpl extends SStreamServiceGrpc.SStreamServiceImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

            HelloResponse response = HelloResponse.newBuilder().setMessage("hello, " + request.getName()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
