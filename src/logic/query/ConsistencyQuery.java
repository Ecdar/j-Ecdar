package logic.query;

import logic.Controller;
import logic.TransitionSystem;

public class ConsistencyQuery extends Query{
    protected ConsistencyQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        TransitionSystem ts = getSystem1();
        setResult(ts.isLeastConsistent());
        if(!getResult()){
            addResultString(ts.getLastErr());
        }
        Controller.saveAutomaton(ts.getAutomaton(), getComponentName());
    }
}
