package logic.query;

import logic.Refinement;
import parser.JSONParser;

public class RefinementQuery extends Query{

    protected RefinementQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        Refinement ref = new Refinement(getSystem1(), getSystem2());
        boolean trace = false; // TODO: handle trace as input?
        boolean refCheck;
        if (trace) {
            refCheck = ref.check(true);
            setResult(refCheck);
            if(refCheck){
                addResultString(JSONParser.writeRefinement(ref.getTree()));
            }
        }
        else {
            setResult(ref.check());
        }

        if (!getResult()) {
            addResultString(ref.getErrMsg());
        }
    }
}
