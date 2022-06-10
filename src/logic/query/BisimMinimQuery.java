package logic.query;

import logic.Bisimilarity;
import logic.Controller;
import models.Automaton;

public class BisimMinimQuery extends Query{
    protected BisimMinimQuery(Query.QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        Automaton aut = getSystem1().getAutomaton();
        aut = Bisimilarity.checkBisimilarity(aut);

        Controller.saveAutomaton(aut, getComponentName());
    }
}
