package connection;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import log.Log;

public class GrpcServer {

    private Server server;

    public GrpcServer(String address) {
        SocketAddress socketAddress = getSocketAddressFromString(address);
        this.server = NettyServerBuilder.forAddress(socketAddress)
                .addService(new EcdarService())
                .build();
    }

    private SocketAddress getSocketAddressFromString(String address) {
        int port = 9000;
        String host = null;
        if (address.indexOf(':') != -1) {
            String[] arr = address.split(":");
            host = arr[0];
            try {
                port = Integer.parseInt(arr[1]);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        } else {
            host = address;
        }
        return new InetSocketAddress(host, port);
    }

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                Log.fatal("Shutting down gRPC server");
                try {
                    GrpcServer.this.stop();
                } catch (InterruptedException e){
                    Log.error("Interrupted while shutting down gRPC server: " + e);
                    Thread.currentThread().interrupt();
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


