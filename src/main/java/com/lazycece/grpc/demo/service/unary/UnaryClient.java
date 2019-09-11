//package com.lazycece.grpc.demo.service.unary;
//
//import com.lazycece.grpc.demo.proto.unary.UnaryServiceGrpc;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * @author lazycece
// * @date 2019/9/11
// */
//public class UnaryClient {
//
//    private static final Logger logger = Logger.getLogger(UnaryClient.class.getName());
//
//    private final ManagedChannel channel;
//    private final UnaryServiceGrpc.UnaryServiceBlockingStub blockingStub;
//
//    /** Construct client connecting to HelloWorld server at {@code host:port}. */
//    public UnaryClient(String host, int port) {
//        this(ManagedChannelBuilder.forAddress(host, port)
//                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
//                // needing certificates.
//                .usePlaintext()
//                .build());
//    }
//
//    /** Construct client for accessing HelloWorld server using the existing channel. */
//    private UnaryClient(ManagedChannel channel) {
//        this.channel = channel;
//        blockingStub = UnaryServiceGrpc.newBlockingStub(channel);
//    }
//
//    public void shutdown() throws InterruptedException {
//        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
//    }
//
//    /** Say hello to server. */
//    public void greet(String name) {
//        logger.info("Will try to greet " + name + " ...");
//        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
//        HelloReply response;
//        try {
//            response = blockingStub.sayHello(request);
//        } catch (StatusRuntimeException e) {
//            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
//            return;
//        }
//        logger.info("Greeting: " + response.getMessage());
//    }
//
//    /**
//     * Greet server. If provided, the first element of {@code args} is the name to use in the
//     * greeting.
//     */
//    public static void main(String[] args) throws Exception {
//        HelloWorldClient client = new HelloWorldClient("localhost", 50051);
//        try {
//      /* Access a service running on the local machine on port 50051 */
//            String user = "world";
//            if (args.length > 0) {
//                user = args[0]; /* Use the arg as the name to greet if provided */
//            }
//            client.greet(user);
//        } finally {
//            client.shutdown();
//        }
//    }
//
//
//}
