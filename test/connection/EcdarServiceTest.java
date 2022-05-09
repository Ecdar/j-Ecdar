package connection;

import EcdarProtoBuf.ComponentProtos;
import EcdarProtoBuf.EcdarBackendGrpc;
import EcdarProtoBuf.QueryProtos;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import models.Automaton;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import parser.JSONParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


public class EcdarServiceTest {

    private InProcessServer<EcdarService> inProcessServer;
    private ManagedChannel channel;
    private EcdarBackendGrpc.EcdarBackendBlockingStub blockingStub;
    private EcdarBackendGrpc.EcdarBackendStub asyncStub;

    @Before
    public void beforeEachTest() throws IOException, InstantiationException, IllegalAccessException {
        inProcessServer = new InProcessServer<EcdarService>(EcdarService.class);
        inProcessServer.start();
        channel = InProcessChannelBuilder
                .forName("test")
                .directExecutor()
                .usePlaintext()
                .build();
        blockingStub = EcdarBackendGrpc.newBlockingStub(channel);
        asyncStub = EcdarBackendGrpc.newStub(channel);
    }

    @After
    public void afterEachTest(){
        channel.shutdownNow();
        inProcessServer.stop();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testUpdateComponent_InvalidJson() throws InterruptedException {
        exceptionRule.expect(StatusRuntimeException.class);
        exceptionRule.expectMessage("ParseException");

        QueryProtos.ComponentsUpdateRequest request = QueryProtos.ComponentsUpdateRequest.newBuilder()
                .addComponents(
                        ComponentProtos.Component.newBuilder()
                                .setJson("\"name\": \"Test12\"")
                                .build())
                .build();

        blockingStub.updateComponents(request);
        shutdown();
    }

    @Test
    public void testSendQuery() throws InterruptedException {
        QueryProtos.ComponentsUpdateRequest request = QueryProtos.ComponentsUpdateRequest.newBuilder()
                .addComponents(ComponentProtos.Component.newBuilder()
                        .setJson(jsonTest1)
                        .build())
                .addComponents(ComponentProtos.Component.newBuilder()
                        .setJson(jsonTest2)
                        .build())
                .addComponents(ComponentProtos.Component.newBuilder()
                        .setJson(jsonTest3)
                        .build())
                .build();
        blockingStub.updateComponents(request);

        QueryProtos.Query query = QueryProtos.Query.newBuilder().setQuery("refinement: (Test1 && Test2) <= Test3").build();
        QueryProtos.QueryResponse queryResponse = blockingStub.sendQuery(query);
        shutdown();

        assertEquals(true, queryResponse.getRefinement().getSuccess());
    }

    @Test
    public void testSendQueryGetComponent_sameComponent() throws InterruptedException, ParseException {
        QueryProtos.ComponentsUpdateRequest request = QueryProtos.ComponentsUpdateRequest.newBuilder()
                .addComponents(ComponentProtos.Component.newBuilder()
                        .setJson(jsonTest1)
                        .build())
                .build();
        blockingStub.updateComponents(request);

        QueryProtos.Query query = QueryProtos.Query.newBuilder().setQuery("get-component: Test1 save-as SavedComponent").build();
        QueryProtos.QueryResponse queryResponse = blockingStub.sendQuery(query);
        shutdown();

        Automaton expected = JSONParser.parseJsonString(jsonTest1, false);
        Automaton actual = JSONParser.parseJsonString(queryResponse.getComponent().getComponent().getJson(), false);
        actual.setName("Test1");
        assertEquals(expected, actual);
    }


    private static String jsonTest1 = "{\r\n  \"name\": \"Test1\",\r\n  \"declarations\": \"clock x;\",\r\n  \"locations\": [\r\n    {\r\n      \"id\": \"L0\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"INITIAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 230.0,\r\n      \"y\": 140.0,\r\n      \"color\": \"5\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    },\r\n    {\r\n      \"id\": \"L3\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"NORMAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 230.0,\r\n      \"y\": 330.0,\r\n      \"color\": \"5\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    }\r\n  ],\r\n  \"edges\": [\r\n    {\r\n      \"sourceLocation\": \"L0\",\r\n      \"targetLocation\": \"L3\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 180.0,\r\n          \"y\": 240.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L3\",\r\n      \"targetLocation\": \"L0\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 240.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L3\",\r\n      \"targetLocation\": \"L3\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 320.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 340.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L0\",\r\n      \"targetLocation\": \"L0\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 130.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 270.0,\r\n          \"y\": 150.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    }\r\n  ],\r\n  \"description\": \"\",\r\n  \"x\": 5.0,\r\n  \"y\": 5.0,\r\n  \"width\": 450.0,\r\n  \"height\": 600.0,\r\n  \"color\": \"5\",\r\n  \"includeInPeriodicCheck\": false\r\n}";
    private static String jsonTest2 = "{\r\n  \"name\": \"Test2\",\r\n  \"declarations\": \"clock y;\",\r\n  \"locations\": [\r\n    {\r\n      \"id\": \"L1\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"INITIAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 190.0,\r\n      \"y\": 140.0,\r\n      \"color\": \"1\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    },\r\n    {\r\n      \"id\": \"L4\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"NORMAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 190.0,\r\n      \"y\": 290.0,\r\n      \"color\": \"1\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    }\r\n  ],\r\n  \"edges\": [\r\n    {\r\n      \"sourceLocation\": \"L1\",\r\n      \"targetLocation\": \"L4\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 220.0,\r\n          \"y\": 210.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L4\",\r\n      \"targetLocation\": \"L1\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 140.0,\r\n          \"y\": 220.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L1\",\r\n      \"targetLocation\": \"L1\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 230.0,\r\n          \"y\": 130.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 230.0,\r\n          \"y\": 150.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L4\",\r\n      \"targetLocation\": \"L4\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 230.0,\r\n          \"y\": 280.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 230.0,\r\n          \"y\": 300.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    }\r\n  ],\r\n  \"description\": \"\",\r\n  \"x\": 5.0,\r\n  \"y\": 5.0,\r\n  \"width\": 450.0,\r\n  \"height\": 600.0,\r\n  \"color\": \"1\",\r\n  \"includeInPeriodicCheck\": false\r\n}";
    private static String jsonTest3 = "{\r\n  \"name\": \"Test3\",\r\n  \"declarations\": \"clock z;\",\r\n  \"locations\": [\r\n    {\r\n      \"id\": \"L2\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"INITIAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 240.0,\r\n      \"y\": 160.0,\r\n      \"color\": \"3\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    },\r\n    {\r\n      \"id\": \"L5\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"NORMAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 120.0,\r\n      \"y\": 320.0,\r\n      \"color\": \"3\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    },\r\n    {\r\n      \"id\": \"L6\",\r\n      \"nickname\": \"\",\r\n      \"invariant\": \"\",\r\n      \"type\": \"NORMAL\",\r\n      \"urgency\": \"NORMAL\",\r\n      \"x\": 310.0,\r\n      \"y\": 320.0,\r\n      \"color\": \"3\",\r\n      \"nicknameX\": 30.0,\r\n      \"nicknameY\": -10.0,\r\n      \"invariantX\": 30.0,\r\n      \"invariantY\": 10.0\r\n    }\r\n  ],\r\n  \"edges\": [\r\n    {\r\n      \"sourceLocation\": \"L2\",\r\n      \"targetLocation\": \"L5\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 180.0,\r\n          \"y\": 240.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L2\",\r\n      \"targetLocation\": \"L6\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 280.0,\r\n          \"y\": 240.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L5\",\r\n      \"targetLocation\": \"L5\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 70.0,\r\n          \"y\": 310.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 70.0,\r\n          \"y\": 330.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L5\",\r\n      \"targetLocation\": \"L6\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 220.0,\r\n          \"y\": 290.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": -10.0,\r\n          \"propertyY\": 10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L6\",\r\n      \"targetLocation\": \"L5\",\r\n      \"status\": \"INPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"a\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 220.0,\r\n          \"y\": 330.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": -10.0,\r\n          \"propertyY\": 10.0\r\n        }\r\n      ]\r\n    },\r\n    {\r\n      \"sourceLocation\": \"L6\",\r\n      \"targetLocation\": \"L6\",\r\n      \"status\": \"OUTPUT\",\r\n      \"select\": \"\",\r\n      \"guard\": \"\",\r\n      \"update\": \"\",\r\n      \"sync\": \"b\",\r\n      \"isLocked\": false,\r\n      \"nails\": [\r\n        {\r\n          \"x\": 350.0,\r\n          \"y\": 310.0,\r\n          \"propertyType\": \"SYNCHRONIZATION\",\r\n          \"propertyX\": 10.0,\r\n          \"propertyY\": -10.0\r\n        },\r\n        {\r\n          \"x\": 350.0,\r\n          \"y\": 330.0,\r\n          \"propertyType\": \"NONE\",\r\n          \"propertyX\": 0.0,\r\n          \"propertyY\": 0.0\r\n        }\r\n      ]\r\n    }\r\n  ],\r\n  \"description\": \"\",\r\n  \"x\": 5.0,\r\n  \"y\": 5.0,\r\n  \"width\": 450.0,\r\n  \"height\": 600.0,\r\n  \"color\": \"3\",\r\n  \"includeInPeriodicCheck\": false\r\n}";
}
