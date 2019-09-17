package com.lazycece.grpc.bs;

import com.lazycece.grpc.bs.proto.BStreamServiceGrpc;
import com.lazycece.grpc.bs.proto.HelloRequest;
import com.lazycece.grpc.bs.proto.HelloResponse;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lazycece
 * @date 2019/09/17
 */
public class BStreamServiceImpl extends BStreamServiceGrpc.BStreamServiceImplBase {

    private static final Logger logger = Logger.getLogger(BStreamServiceImpl.class.getName());

    @Override
    public StreamObserver<HelloRequest> sayHello(StreamObserver<HelloResponse> responseObserver) {
        // 禁用自动接收流控制
        final ServerCallStreamObserver<HelloResponse> serverCallStreamObserver = (ServerCallStreamObserver<HelloResponse>) responseObserver;
        serverCallStreamObserver.disableAutoInboundFlowControl();

        final AtomicBoolean wasReady = new AtomicBoolean(false);
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            public void run() {
                if (serverCallStreamObserver.isReady() && wasReady.compareAndSet(false, true)) {
                    logger.info("READY");
                    // Signal the request sender to send one message. This happens when isReady() turns true, signaling that
                    // the receive buffer has enough free space to receive more messages. Calling request() serves to prime
                    // the message pump.
                    serverCallStreamObserver.request(1);
                }
            }
        });

        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest request) {
                try {
                    logger.info("request user is --> " + request.getName());
                    // Simulate server "work"
                    Thread.sleep(100);

                    responseObserver.onNext(
                            HelloResponse.newBuilder()
                                    .setMessage("hello, " + request.getName())
                                    .build());

                    // 判断ServerCallStreamObserver是否仍然是可以接收消息的ready状态
                    if (serverCallStreamObserver.isReady()) {
                        // Signal the sender to send another request. As long as isReady() stays true, the server will keep
                        // cycling through the loop of onNext() -> request()...onNext() -> request()... until either the client
                        // runs out of messages and ends the loop or the server runs out of receive buffer space.
                        //
                        // If the server runs out of buffer space, isReady() will turn false. When the receive buffer has
                        // sufficiently drained, isReady() will turn true, and the serverCallStreamObserver's onReadyHandler
                        // will be called to restart the message pump.
                        serverCallStreamObserver.request(1);
                    } else {
                        wasReady.set(false);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    responseObserver.onError(
                            Status.UNKNOWN
                                    .withDescription("Error handling request")
                                    .withCause(throwable)
                                    .asException()
                    );
                }
            }

            @Override
            public void onError(Throwable t) {
                // 如果客户端出现错误，则结束响应流。
                logger.log(Level.WARNING, t.getMessage());
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // 当客户端结束请求流的时候，发出工作结束的信号。
                logger.info("on completed");
                responseObserver.onCompleted();
            }
        };
    }
}
