package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Quotient extends TransitionSystem {

    private final TransitionSystem ts1, ts2;
    private final Set<Channel> inputs, outputs;
    private Clock newClock;

    public Quotient(TransitionSystem ts1, TransitionSystem ts2) {
        this.ts1 = ts1;
        this.ts2 = ts2;

        //clocks should contain the clocks of ts1, ts2 and a new clock
        newClock = new Clock("new");
        clocks.add(newClock);
        clocks.addAll(ts1.getClocks());
        clocks.addAll(ts2.getClocks());

        // inputs should contain inputs of ts1, outputs of ts2 and a new input
        inputs = new HashSet<>(ts1.getInputs());
        inputs.addAll(ts2.getOutputs());
        inputs.add(new Channel("newInput"));

        Set<Channel> outputsOF1 = new HashSet<>(ts1.getOutputs());
        outputsOF1.addAll(ts1.getSyncs());
        Set<Channel> outputsOF2 = new HashSet<>(ts2.getOutputs());
        outputsOF1.addAll(ts2.getSyncs());
        outputs = new HashSet<>(outputsOF1);
        outputs.removeAll(outputsOF2);
        System.out.println();
    }

    public SymbolicLocation getInitialLocation() {
        // the invariant of locations consisting of locations from each transition system should be true
        // which means the location has no invariants
        SymbolicLocation initLoc = getInitialLocation(new TransitionSystem[]{ts1, ts2});
        ((ComplexLocation) initLoc).removeInvariants();
        return initLoc;
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public List<TransitionSystem> getSystems(){
        List<TransitionSystem> result = new ArrayList<>();
        result.addAll(ts1.getSystems());
        result.addAll(ts2.getSystems());
        return result;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel) {
        SymbolicLocation location = currentState.getLocation();

        List<Move> moves = getNextMoves(location, channel);
        return createNewTransitions(currentState, moves);
    }

    public List<Move> getNextMoves(SymbolicLocation location, Channel channel) {
        List<Move> resultMoves = new ArrayList<>();

        if (location instanceof ComplexLocation) {
            List<SymbolicLocation> locations = ((ComplexLocation) location).getLocations();

            // symbolic locations corresponding to each TS
            SymbolicLocation loc1 = locations.get(0);
            SymbolicLocation loc2 = locations.get(1);

            // rule 2
            Move newMove = new Move(location, new InconsistentLocation(), new ArrayList<>());
            // invariant is negation of invariant of ts1 and invariant of ts2
            List<Guard> newGuards = new ArrayList<>(loc2.getInvariants());
            newGuards.addAll(loc1.getInvariants().stream().map(Guard::negate).collect(Collectors.toList()));
            newMove.setGuards(newGuards);
            newMove.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
            resultMoves.add(newMove);

            // rule 1
            if (getActions().contains(channel)) {
                Move newMove1 = new Move(location, new UniversalLocation(), new ArrayList<>());
                // negate invariant of ts2
                newMove1.setGuards(loc2.getInvariants().stream().map(Guard::negate).collect(Collectors.toList()));
                newMove1.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
                resultMoves.add(newMove1);
            }

            // rule 3 (cartesian product)
            if (ts1.getActions().contains(channel) && ts2.getActions().contains(channel)) {
                List<Move> movesFrom1 = getNextMoves(loc1, channel);
                List<Move> movesFrom2 = getNextMoves(loc2, channel);

                if (!movesFrom1.isEmpty() && !movesFrom2.isEmpty()) {
                    List<Move> moveProduct = moveProduct(movesFrom1, movesFrom2, true);
                    resultMoves.addAll(moveProduct);
                }
            }

            // rule 4 and 6
            if (ts2.getOutputs().contains(channel)) {
                List<Move> movesFrom1 = getNextMoves(loc1, channel);
                List<Move> movesFrom2 = getNextMoves(loc2, channel);

                // take all moves from ts1 in order to gather the guards and negate them
                List<Guard> newGuards2 = movesFrom1.stream().map(Move::getGuards).flatMap(List::stream).map(Guard::negate).collect(Collectors.toList());

                for (Move move : movesFrom2) {
                    Move newMove2 = new Move(location, new InconsistentLocation(), new ArrayList<>());
                    List<Guard> newGuards3 = new ArrayList<>(move.getGuards());
                    newGuards3.addAll(newGuards2);
                    newMove2.setGuards(newGuards3);
                    newMove2.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
                    resultMoves.add(newMove2);
                }

                // take all moves from ts2 in order to gather the guards and negate them
                List<Guard> newGuards4 = movesFrom2.stream().map(Move::getGuards).flatMap(List::stream).map(Guard::negate).collect(Collectors.toList());

                // doesn't make sense since we don't use anything from movesFrom1
                for (Move move : movesFrom1) {
                    Move newMove4 = new Move(location, new UniversalLocation(), new ArrayList<>());
                    newMove4.setGuards(newGuards4);
                    resultMoves.add(newMove4);
                }
            }

            // rule 5
            if (!ts2.getActions().contains(channel)) {
                List<Move> movesFrom1 = getNextMoves(loc1, channel);

                for (Move move : movesFrom1) {
                    SymbolicLocation newLoc = new ComplexLocation(new ArrayList<>(Arrays.asList(move.getTarget(), loc2)));
                    Move newMove3 = new Move(location, newLoc, new ArrayList<>());
                    newMove3.setGuards(move.getGuards());
                    newMove3.setUpdates(move.getUpdates());
                    resultMoves.add(newMove3);
                }

            }
        } else if (location instanceof InconsistentLocation) {
            if (getInputs().contains(channel)) {
                Move newMove = new Move(location, new InconsistentLocation(), new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new Update(newClock, 0))));
                resultMoves.add(newMove);
            }
        } else if (location instanceof UniversalLocation) {
            if (getActions().contains(channel)) {
                Move newMove = new Move(location, new UniversalLocation(), new ArrayList<>());
                resultMoves.add(newMove);
            }
        }

        return resultMoves;
    }
}
