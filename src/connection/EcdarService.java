package connection;

import EcdarProtoBuf.ComponentProtos;
import EcdarProtoBuf.EcdarBackendGrpc;
import EcdarProtoBuf.QueryProtos;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import logic.Controller;

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
        } catch (Exception e){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getClass().getName() + ": " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void sendQuery(QueryProtos.Query request,
                          StreamObserver<QueryProtos.QueryResponse> responseObserver) {
        try {
            String response = Controller.handleRequest(request.getQuery());
            QueryProtos.QueryResponse queryResponse = QueryProtos.QueryResponse.newBuilder().setQuery(
                    QueryProtos.Query.newBuilder().setQuery(response).build()
            ).build();
            responseObserver.onNext(queryResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getClass().getName() + ": " + e.getMessage()).asRuntimeException());
        }
    }
}
