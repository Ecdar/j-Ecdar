package logic;

import log.Log;
import models.*;

import java.util.*;

public class Quotient extends AggregatedTransitionSystem {
    private final TransitionSystem t, s;
    private final Set<Channel> inputs, outputs;
    private final Channel newChan;
    private final Clock newClock;

    public Quotient(TransitionSystem t, TransitionSystem s) {
        super(t, s);

        this.t = t;
        this.s = s;

        // Clocks should contain the clocks of t, s, and the new clock.
        Optional<Clock> existingClock = clocks.findAnyWithOriginalName("quo_new");
        if (existingClock.isPresent()) {
            newClock = existingClock.get();
        } else {
            newClock = new Clock("quo_new", "quo", true);
            clocks.add(newClock);
        }

        // Act_i = Act_i^T ∪ Act_o^S
        inputs = union(t.getInputs(), s.getOutputs());
        newChan = new Channel("i_new");
        inputs.add(newChan);

        // Act_o = Act_o^T \ Act_o^S ∪ Act_i^S \ Act_i^T
        outputs = union(
                difference(t.getOutputs(), s.getOutputs()),
                difference(s.getInputs(), t.getInputs())
        );
    }

    public Quotient(Automaton t, Automaton s) {
        this(new SimpleTransitionSystem(t), new SimpleTransitionSystem(s));
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    @Override
    public String getName() {
        return t.getName() + "\\\\" + s.getName();
    }

    @Override
    public List<Move> getNextMoves(Location location, Channel a) {
        Location univ = Location.createUniversalLocation("universal", 0, 0);
        Location inc = Location.createInconsistentLocation("inconsistent", 0, 0, newClock);

        List<Move> resultMoves = new ArrayList<>();

        // Rule 10
        if (location.isInconsistent()) {
            if (getInputs().contains(a)) {
                Log.debug("Rule 10");
                Move newMove = new Move(location, location, new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove);
            }
        }

        // Rule 9
        if (location.isUniversal()) {
            if (getActions().contains(a)) {
                Log.debug("Rule 9");
                Move newMove = new Move(location, location, new ArrayList<>());
                resultMoves.add(newMove);
            }
        }

        if (location.isComposed()) {
            List<Location> locations = location.getChildren();

            // Symbolic locations corresponding to each TS.
            Location lt = locations.get(0);
            Location ls = locations.get(1);

            List<Move> t_moves = t.getNextMoves(lt, a);
            List<Move> s_moves = s.getNextMoves(ls, a);

            // Rule 1 (cartesian product)
            if (in(a, intersect(s.getActions(), t.getActions()))) {
                Log.debug("Rule 1");
                List<Move> moveProduct = moveProduct(t_moves, s_moves, true, true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // Rule 2
            if (in(a, difference(s.getActions(), t.getActions()))) {
                Log.debug("Rule 2");
                List<Move> movesLeft = new ArrayList<>();
                movesLeft.add(new Move(lt, lt, new ArrayList<>()));

                List<Move> moveProduct = moveProduct(movesLeft, s_moves, true, true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // Rule 3
            // Rule 4
            // Rule 5
            if (in(a, s.getOutputs())) {
                Log.debug("Rule 345 1");
                CDD guard_s = CDD.cddFalse();
                for (Move s_move : s_moves) {
                    guard_s = guard_s.disjunction(s_move.getEnabledPart());
                }
                guard_s = guard_s.negation().removeNegative().reduce();

                CDD inv_neg_inv_loc_s = ls.getInvariantCdd().negation().removeNegative().reduce();

                CDD combined = guard_s.disjunction(inv_neg_inv_loc_s);

                Move move = new Move(location, univ, new ArrayList<>());
                move.conjunctCDD(combined);
                resultMoves.add(move);
            } else {
                Log.debug("Rule 345 2");
                CDD inv_neg_inv_loc_s = ls.getInvariantCdd().negation().removeNegative().reduce();

                Move move = new Move(location, univ);
                move.conjunctCDD(inv_neg_inv_loc_s);
                resultMoves.add(move);
            }

            // Rule 6
            if (in(a, intersect(t.getOutputs(), s.getOutputs()))) {
                Log.debug("Rule 6");
                // Take all moves from t in order to gather the guards and negate them.
                CDD CDDFromMovesFromT = CDD.cddFalse();
                for (Move t_move : t_moves) {
                    CDDFromMovesFromT = CDDFromMovesFromT.disjunction(t_move.getEnabledPart());
                }
                CDD negated = CDDFromMovesFromT.negation().removeNegative().reduce();

                for (Move s_move : s_moves) {
                    Move newMoveRule6 = new Move(location, inc, new ArrayList<>());
                    newMoveRule6.setGuards(s_move.getEnabledPart().conjunction(negated));
                    newMoveRule6.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                    resultMoves.add(newMoveRule6);
                }
            }

            // Rule 7
            if (Objects.equals(a.getName(), this.newChan.getName())) {
                Log.debug("Rule 7");
                Move newMoveRule7 = new Move(location, inc, new ArrayList<>());
                // Invariant is negation of invariant of t conjoined with invariant of s
                CDD negatedInvar = lt.getInvariantCdd().negation();
                CDD combined = negatedInvar.conjunction(ls.getInvariantCdd());

                newMoveRule7.setGuards(combined);
                newMoveRule7.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMoveRule7);
            }

            // Rule 8
            if (in(a, difference(t.getActions(), s.getActions()))) {
                Log.debug("Rule 8");
                List<Move> movesRight = new ArrayList<>();
                movesRight.add(new Move(ls, ls, new ArrayList<>()));
                List<Move> moveProduct = moveProduct(t_moves, movesRight, true, true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }
        }

        return resultMoves;
    }
}
