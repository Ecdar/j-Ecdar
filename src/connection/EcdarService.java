package connection;

import EcdarProtoBuf.ComponentProtos;
import EcdarProtoBuf.EcdarBackendGrpc;
import EcdarProtoBuf.QueryProtos;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import logic.Controller;
import logic.query.Query;
import models.CDD;

import java.util.List;

public class EcdarService extends EcdarBackendGrpc.EcdarBackendImplBase {



    @Override
    public void updateComponents(QueryProtos.ComponentsUpdateRequest request,
                                 StreamObserver<Empty> responseObserver) {
        try{
            List<ComponentProtos.Component> components = request.getComponentsList();
            for (ComponentProtos.Component component: components) {
                switch (component.getRepCase()){
                    case JSON : Controller.parseComponentJson(component.getJson()); break;
                    case XML : Controller.parseComponentXml(component.getXml());
                }
            }
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Throwable e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(tryEnsureDone(e)).asRuntimeException());
        }
    }

    @Override
    public void sendQuery(QueryProtos.Query request,
                          StreamObserver<QueryProtos.QueryResponse> responseObserver) {
        try {
            Query response = Controller.handleRequest(request.getQuery());
            QueryProtos.QueryResponse.Builder queryResponseBuilder = QueryProtos.QueryResponse.newBuilder().setQuery(
                    QueryProtos.Query.newBuilder().setQuery(request.getQuery()).build()
            );

            switch (response.getType()){
                case REFINEMENT:
                    queryResponseBuilder.setRefinement(
                            QueryProtos.QueryResponse.RefinementResult.newBuilder()
                                    .setSuccess(response.getResult()).build()
                    );
                    break;
                case CONSISTENCY:
                    queryResponseBuilder.setConsistency(
                            QueryProtos.QueryResponse.ConsistencyResult.newBuilder()
                                    .setSuccess(response.getResult()).build()
                    );
                    break;
                case IMPLEMENTATION:
                    queryResponseBuilder.setImplementation(
                            QueryProtos.QueryResponse.ImplementationResult.newBuilder()
                                    .setSuccess(response.getResult()).build()
                    );
                    break;
                case DETERMINISM:
                    queryResponseBuilder.setDeterminism(
                            QueryProtos.QueryResponse.DeterminismResult.newBuilder()
                                    .setSuccess(response.getResult()).build()
                    );
                    break;
                case GET_COMPONENT:
                case BISIM_MINIM:
                case PRUNE:
                    getComponent(response, queryResponseBuilder);
                    break;
                default:
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Query has an invalid type").asRuntimeException());
            }
            responseObserver.onNext(queryResponseBuilder.build());
            responseObserver.onCompleted();
        } catch (Throwable e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(tryEnsureDone(e)).asRuntimeException());
        }
    }

    private String tryEnsureDone(Throwable e) {
        String description = e.getClass().getName() + ": " + e.getMessage();

        try {CDD.ensureDone(); }
        catch (Throwable ee) {
            description += "\n" + ee.getClass().getName() + ": " + ee.getMessage();
        }

        return description;
    }

    private void getComponent(Query response, QueryProtos.QueryResponse.Builder queryResponseBuilder) {
        String jsonComponent = Controller.getJsonComponent(response.getComponentName());
        queryResponseBuilder.setComponent(
                QueryProtos.QueryResponse.ComponentResult.newBuilder()
                        .setComponent(ComponentProtos.Component.newBuilder().setJson(jsonComponent).build()).build()
        );
    }
}
