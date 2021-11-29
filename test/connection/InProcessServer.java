package connection;

import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class InProcessServer<T extends io.grpc.BindableService> {
    private static final Logger logger = Logger.getLogger(InProcessServer.class.getName());

    private Server server;

    private Class<T> clazz;

    public InProcessServer(Class<T> clazz){
        this.clazz = clazz;
    }

    public void start() throws IOException, InstantiationException, IllegalAccessException {
        server = InProcessServerBuilder
                .forName("test")
                .directExecutor()
                .addService(clazz.newInstance())
                .build()
                .start();
        logger.info("InProcessServer started.");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                InProcessServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
