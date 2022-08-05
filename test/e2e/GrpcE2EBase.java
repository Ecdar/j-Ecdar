package e2e;

import EcdarProtoBuf.ComponentProtos;
import connection.EcdarService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GrpcE2EBase {
    private final String componentsFolder;

    private Server server;
    private ManagedChannel channel;
    private EcdarProtoBuf.EcdarBackendGrpc.EcdarBackendBlockingStub stub;

    public GrpcE2EBase(String componentsFolder) {
        this.componentsFolder = componentsFolder;
    }

    @Before
    public void beforeEachTest()
            throws NullPointerException, IOException {
        // Finds all the json components in the university component folder
        File componentsFolder = new File(this.componentsFolder);
        File[] componentFiles = componentsFolder.listFiles();

        assertNotNull(componentFiles);
        assertEquals(componentFiles.length, 9);

        // Find all the components stored as json and create a component for it
        List<ComponentProtos.Component> components = new ArrayList<>();
        for (File componentFile : componentFiles) {
            String contents = Files.readString(componentFile.toPath());

            ComponentProtos.Component component = ComponentProtos.Component
                    .newBuilder()
                    .setJson(contents)
                    .build();
            components.add(component);
        }

        // Creates the server and associated channel, which both must have the same name
        String name = this.getClass().getName();
        server = InProcessServerBuilder
                .forName(name)
                .directExecutor()
                .addService(new EcdarService())
                .build()
                .start();
        channel = InProcessChannelBuilder
                .forName(name)
                .directExecutor()
                .usePlaintext()
                .build();

        // Creates the stub which allows us to use function calls for remote procedures
        stub = EcdarProtoBuf.EcdarBackendGrpc.newBlockingStub(channel);

        // Creates all the components such that they can be utilised in the tests later
        EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest request = EcdarProtoBuf.QueryProtos.ComponentsUpdateRequest
                .newBuilder()
                .addAllComponents(components)
                .build();
        stub.updateComponents(request);
    }

    @After
    public void afterEachTest() {
        this.channel.shutdownNow();
        this.server.shutdown();
    }

    protected EcdarProtoBuf.EcdarBackendGrpc.EcdarBackendBlockingStub getStub() {
        return stub;
    }

    protected EcdarProtoBuf.QueryProtos.Query createQuery(String query) {
        return EcdarProtoBuf.QueryProtos.Query.newBuilder()
                .setQuery(query)
                .build();
    }

    protected EcdarProtoBuf.QueryProtos.QueryResponse query(String query) {
        return stub.sendQuery(
                createQuery(query)
        );
    }

    protected boolean consistency(String query) {
        EcdarProtoBuf.QueryProtos.QueryResponse response = query(query);
        return response.getConsistency().getSuccess();
    }

    protected boolean refinement(String query) {
        EcdarProtoBuf.QueryProtos.QueryResponse response = query(query);
        return response.getRefinement().getSuccess();
    }

    protected String getComponent(String query) {
        EcdarProtoBuf.QueryProtos.QueryResponse response = query(query);
        return response.getComponent().getComponent().getJson();
    }
}
