package connection;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GrpcServer {

    private Server server;

    public GrpcServer(int port) {
        this.server = ServerBuilder.forPort(port)
                .addService(new EcdarService())
                .build();
    }

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                System.err.println("Shutting down gRPC server");
                try {
                    GrpcServer.this.stop();
                } catch (InterruptedException e){
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    private void stop() throws InterruptedException{
        if(server != null){
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}


