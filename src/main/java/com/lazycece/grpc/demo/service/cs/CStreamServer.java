package com.lazycece.grpc.demo.service.cs;

import com.lazycece.grpc.demo.proto.cs.CStreamServiceGrpc;
import com.lazycece.grpc.demo.proto.cs.HelloRequest;
import com.lazycece.grpc.demo.proto.cs.HelloResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/9/11
 */
public class CStreamServer {

    private static final Logger logger = Logger.getLogger(CStreamServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new CStreamServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                CStreamServer.this.stop();
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
        final CStreamServer cStreamServer = new CStreamServer();
        cStreamServer.start();
        cStreamServer.blockUntilShutdown();
    }

    static class CStreamServiceImpl extends CStreamServiceGrpc.CStreamServiceImplBase {

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
}
