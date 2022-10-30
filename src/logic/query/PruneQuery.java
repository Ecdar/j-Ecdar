package logic.query;

import logic.Controller;
import logic.Pruning;
import logic.SimpleTransitionSystem;
import models.Automaton;

public class PruneQuery extends Query{
    protected PruneQuery(QueryBuilder builder) {
        super(builder);
    }

    @Override
    public void handle() {
        Automaton aut = getSystem1().getAutomaton();

        SimpleTransitionSystem simp = Pruning.adversarialPruning(new SimpleTransitionSystem(aut));
        aut = simp.pruneReachTimed().getAutomaton();

        Controller.saveAutomaton(aut, getComponentName());
    }
}
