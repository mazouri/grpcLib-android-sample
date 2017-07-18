package com.mazouri.grpcLib_android_sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;

/**
 * Created by wangdongdong on 17/6/29.
 */

public class HelloServer {

    private int port = 8080;
    private boolean useTls = false;

    private ScheduledExecutorService executor;
    private Server server;

    public static void main(String[] args ) throws Exception {
        final HelloServer server = new HelloServer();
//        server.parseArgs(args);
        if (server.useTls) {
            System.out.println(
                    "\nUsing fake CA for TLS certificate. Test clients should expect host\n"
                            + "*.test.google.fr and our test CA. For the Java test client binary, use:\n"
                            + "--server_host_override=foo.test.google.fr --use_test_ca=true\n");
        }
        server.start();
        server.blockUnitilShutdown();
    }

    private void parseArgs(String[] args) {
        boolean usage = false;
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                System.err.println("All arguments must start with '--': " + arg);
                usage = true;
                break;
            }
            String[] parts = arg.substring(2).split("=", 2);
            String key = parts[0];
            if ("help".equals(key)) {
                usage = true;
                break;
            }
            if (parts.length != 2) {
                System.err.println("All arguments must be of the form --arg=value");
                usage = true;
                break;
            }
            String value = parts[1];
            if ("port".equals(key)) {
                port = Integer.parseInt(value);
            } else if ("use_tls".equals(key)) {
                useTls = Boolean.parseBoolean(value);
            } else if ("grpc_version".equals(key)) {
                if (!"2".equals(value)) {
                    System.err.println("Only grpc version 2 is supported");
                    usage = true;
                    break;
                }
            } else {
                System.err.println("Unknown argument: " + key);
                usage = true;
                break;
            }
        }
        if (usage) {
            HelloServer s = new HelloServer();
            System.out.println(
                    "Usage: [ARGS...]"
                            + "\n"
                            + "\n  --port=PORT           Port to connect to. Default " + s.port
                            + "\n  --use_tls=true|false  Whether to use TLS. Default " + s.useTls
            );
            System.exit(1);
        }
    }

    private void start() throws Exception {
        executor = Executors.newSingleThreadScheduledExecutor();
        server = ServerBuilder.forPort(port)  //host:ip:10.75.75.106
                .addService(new GreeterImpl())
                .build().start();

        System.out.println("HelloServer started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Shutting down");
                    HelloServer.this.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void blockUnitilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloResponse> responseObserver) {
            HelloResponse reply = HelloResponse.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }


    }
}
