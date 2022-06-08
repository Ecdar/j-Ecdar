package logic.query;

import logic.Controller;
import logic.TransitionSystem;

public class GetComponentQuery extends Query{
    protected GetComponentQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        TransitionSystem ts = getSystem1();
        Controller.saveAutomaton(ts.getAutomaton(), getComponentName());
    }
}
