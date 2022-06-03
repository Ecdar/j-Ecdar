package logic.query;

import logic.Controller;
import logic.TransitionSystem;

public class ImplementationQuery extends Query{
    protected ImplementationQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        TransitionSystem ts = getSystem1();
        setResult(ts.isImplementation());
        if(!getResult()){
            addResultString(ts.getLastErr());
        }
        Controller.saveAutomaton(ts.getAutomaton(), getComponentName());
    }
}
