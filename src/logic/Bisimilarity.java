package logic;


import models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Bisimilarity {

    static boolean thereWasAChange = true;


    public static Automaton checkBisimilarity(Automaton aut1) {
        Automaton copy = new Automaton(aut1);


        List<Location> locs = copy.getLocations();
        List<Edge> edges = copy.getEdges();
        List<Clock> clocks = copy.getClocks();
        List<BoolVar> BVs = copy.getBVs();
        List<List<Location>> bisimilarLocs = new ArrayList<>(); // we will create a list of "lists of bisimilar locations"
        bisimilarLocs.add(locs); // at the start we "assume all locs are bisimilar"


        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(clocks);
        CDD.addBddvar(BVs);
        thereWasAChange= true;


        while (thereWasAChange) {
            thereWasAChange=false;
            List<List<Location>> splitOffList = new ArrayList<>();
            for (List<Location>  locationList : bisimilarLocs )
            {
                if (locationList.isEmpty() || locationList.size() == 1) // cant split any more locations from the current list
                    continue;
                List<Location> splitOffPart = new ArrayList<>();

                for (int i=0; i<locationList.size(); i++) {

                    if (thereWasAChange==false) {

                        Location firstLoc = locationList.get(i);

                        for (int j = i; j < locationList.size(); j++) {

                            Location l = locationList.get(j);
                            if (hasDifferentZone(l, firstLoc, clocks)) {
                                if (!splitOffPart.contains(l)) splitOffPart.add(l);
                                thereWasAChange = true;
                            } else if (hasDifferentOutgoings(l, firstLoc, clocks, edges, bisimilarLocs)) {
                                if (!splitOffPart.contains(l)) splitOffPart.add(l);
                                thereWasAChange = true;
                            }
                        }
                    }

                }
                if (!splitOffPart.isEmpty()) {
                    for (Location l : splitOffPart)
                        locationList.remove(l);
                    splitOffList.add(splitOffPart);
                }
            }
            bisimilarLocs.addAll(splitOffList);
        }

        locs = new ArrayList<>();

        for (List<Location> list : bisimilarLocs) { // for each list of bisimilar locks, we chose one (the first) to keep, and replace all the others with that one in any edge.
            Location chosen = list.get(0);
            locs.add(chosen);
            list.remove(chosen);
            for (Location l : list)
            {
                  for (Edge e : edges) {
                      if (e.getTarget().equals(l))
                          e.setTarget(chosen);
                      if (e.getSource().equals(l))
                          e.setSource(chosen);
                  }
            }
        }
        List<Edge> finalEdges = new ArrayList<>();


        for (Location l: locs) // now we merge all the edges that have a similar source location, action, target and update
        {
            for (Channel c : copy.getActions())
            {
                for (Location targetLoc : locs) {

                    boolean thereWasAnEdge= false;
                    if (!edges.stream().filter(e -> e.getSource().equals(l) && e.getChannel().equals(c) && e.getTarget().equals(targetLoc) ).collect(Collectors.toList()).isEmpty()) {
                        thereWasAnEdge=true;
                    }


                    if (thereWasAnEdge) {
                        List<Edge> allEdges =edges.stream().filter(e -> e.getSource().equals(l) && e.getChannel().equals(c) && e.getTarget().equals(targetLoc)).collect(Collectors.toList());

                        List<List<Edge>> edgesWithSimilarUpdates = new ArrayList<>();
                        for (Edge e: allEdges)
                        {
                            boolean foundAListWithSameUpdate = false;
                            for (List<Edge> edgeList : edgesWithSimilarUpdates)
                            {
                                if (!edgeList.isEmpty() && Arrays.equals(edgeList.get(0).getUpdates().toArray(),e.getUpdates().toArray()))
                                {
                                    edgeList.add(e);
                                    foundAListWithSameUpdate=true;
                                }
                            }
                            if (!foundAListWithSameUpdate) {
                                List<Edge> newEdgeList = new ArrayList<>() {{
                                    add(e);
                                }};
                                edgesWithSimilarUpdates.add(newEdgeList);
                            }

                        }

                        for (List<Edge> edgeList : edgesWithSimilarUpdates) {
                            List<Update> updates = edgeList.get(0).getUpdates();
                            CDD allCDDs = CDD.cddFalse();
                            for (Edge e : edgeList) {
                                CDD targetFedAfterReset = e.getTarget().getInvariantCDD();
                                targetFedAfterReset = CDD.applyReset(targetFedAfterReset,e.getUpdates());
                                allCDDs = allCDDs.disjunction(e.getGuardCDD().conjunction(targetFedAfterReset));

                                assert (Arrays.equals(Arrays.stream(updates.toArray()).toArray(), Arrays.stream(e.getUpdates().toArray()).toArray()));
                            }
                            finalEdges.add(new Edge(l, targetLoc, c,  edgeList.get(0).isInput(), allCDDs.getGuard(copy.getClocks()), allEdges.get(0).getUpdates()));
                        }

                    }

                }
            }
        }

        CDD.done();
        return new Automaton(copy.getName()+"Bisimilar",locs,finalEdges,clocks, copy.getBVs());

    }

    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.hashCode() == clocks.get(i).hashCode()) return i + 1;
        }
        return 0;
    }

    public static boolean hasDifferentZone(Location l1, Location l2, List<Clock> clocks)
    {
        if (l1.getInvariantCDD().equiv(l2.getInvariantCDD())) {
            return false;
        }
        return true;
    }

    public static boolean hasDifferentOutgoings(Location l1, Location l2, List<Clock> clocks, List<Edge> edges, List<List<Location>> bisimilarLocs)
    {
        List<Edge> edgesL1 = new ArrayList<>();
        List<Edge> edgesL2 = new ArrayList<>();

        for (Edge e: edges)
        {
            if (e.getSource().equals(l1))
                edgesL1.add(e);
            if (e.getSource().equals(l2))
                edgesL2.add(e);
        }

        CDD s1 = l1.getInvariantCDD();
        CDD s2 = l2.getInvariantCDD();

        for (Edge e1 : edgesL1)
        {
            Channel c = e1.getChan();
            if (edgesL2.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()).isEmpty()) {
                return true;
            }
            CDD e1CDD = s1.conjunction(e1.getGuardCDD());
            CDD e2CDD = null;
            for (Edge e2 : edgesL2.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()))
            {
                if (e2.getChannel().equals(c))
                {
                    if (e2CDD==null)
                        e2CDD = s2.conjunction(e2.getGuardCDD());
                    else
                        e2CDD = s2.conjunction(e2.getGuardCDD()).disjunction(e2CDD);
                }
                if (CDD.intersects(e1CDD,s2.conjunction(e2.getGuardCDD())) && !Arrays.equals(Arrays.stream(e1.getUpdates().toArray()).toArray(), Arrays.stream(e2.getUpdates().toArray()).toArray()))
                {
                    return true;
                }


               if (CDD.intersects(e1CDD,s2.conjunction(e2.getGuardCDD())) && getIndexInBislimlarLocs(e1.getTarget(), bisimilarLocs)!=getIndexInBislimlarLocs(e2.getTarget(),bisimilarLocs))
                {
                    if (l1.getName().equals(l2.getName())) {
                    }
                    return true;
                }
            }
            if (!CDD.isSubset(e1CDD,e2CDD)) {
                return true;
            }
        }


        for (Edge e2 : edgesL2)
        {
            Channel c = e2.getChan();
           // System.out.println(c);
            //if (c.getName().equals("c[0]"))
           //     System.out.println("this i did reach" + edgesL1.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()) );
            if (edgesL1.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()).isEmpty()) {
                return true;
            }
            CDD e2CDD = s2.conjunction(e2.getGuardCDD());
            CDD e1CDD = null;
            for (Edge e1 : edgesL1.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()))
            {
                if (e1.getChannel().equals(c))
                {
                    if (e1CDD==null)
                        e1CDD = s1.conjunction(e1.getGuardCDD());
                    else
                        e1CDD = s1.conjunction(e1.getGuardCDD()).disjunction(e1CDD);
                }

                if (CDD.intersects(e2CDD,s1.conjunction(e1.getGuardCDD())) && getIndexInBislimlarLocs(e1.getTarget(),bisimilarLocs)!=getIndexInBislimlarLocs(e2.getTarget(),bisimilarLocs))
                {

                    return true;
                }


            }
            if (!CDD.isSubset(e2CDD, e1CDD)) {
                return true;
            }
        }




        return false;
    }

    private static int getIndexInBislimlarLocs(Location l1, List<List<Location>> bisimilarLocs ) {
        int res=0;
        for (int i=0; i<bisimilarLocs.size();i++)
        {
            if (bisimilarLocs.get(i).contains(l1))
                res = i;
        }

        return res;


    }


}
