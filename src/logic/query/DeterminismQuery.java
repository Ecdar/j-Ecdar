package logic.query;

import logic.Controller;
import logic.TransitionSystem;

public class DeterminismQuery extends Query{
    protected DeterminismQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        TransitionSystem ts = getSystem1();
        setResult(ts.isDeterministic());
        if(!getResult()){
            addResultString(ts.getLastErr());
        }
        Controller.saveAutomaton(ts.getAutomaton(), getComponentName());
    }
}
