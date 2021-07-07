package logic;


import lib.DBMLib;
import models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Bisimilarity {

    static boolean thereWasAChange = true;


    public static Automaton checkBisimilarity(Automaton aut) {
        List<Location> locs = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        List<Clock> clocks = aut.getClocks();
        locs.addAll(aut.getLocations());
        edges.addAll(aut.getEdges());
        List<List<Location>> bisimilarLocs = new ArrayList<>();
        bisimilarLocs.add(locs);





        while (thereWasAChange) {
            thereWasAChange=false;
            List<List<Location>> splitOffList = new ArrayList<>();
            for (List<Location>  locationList : bisimilarLocs )
            {
                if (locationList.isEmpty() || locationList.size() == 1)
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
                System.out.println("split off " + splitOffPart);
                if (!splitOffPart.isEmpty()) {
                    for (Location l : splitOffPart)
                        locationList.remove(l);
                    splitOffList.add(splitOffPart);
                }



            }
            System.out.println("passed for list");
            bisimilarLocs.addAll(splitOffList);


            System.out.println(bisimilarLocs);
        }

        locs = new ArrayList<>();

        for (List<Location> list : bisimilarLocs) {
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


        for (Location l: locs)
        {
            for (Channel c : aut.getActions())
            {
                for (Location targetLoc : locs) {
                    Federation allFeds = null;
                    boolean thereWasAnEdge= false;
                    for (Edge e : edges.stream().filter(e -> e.getSource().equals(l) && e.getChannel().equals(c) && e.getTarget().equals(targetLoc)).collect(Collectors.toList())) {
                        thereWasAnEdge=true;
                        Federation targetFedAfterReset = e.getTarget().getInvariantFederation(clocks);
                        for (Update u: e.getUpdates())
                            targetFedAfterReset= targetFedAfterReset.free(getIndexOfClock(u.getClock(),clocks));

                        if (allFeds==null)
                            allFeds= e.getGuardFederation(clocks).intersect(targetFedAfterReset);
                        else
                            allFeds = Federation.fedPlusFed(allFeds,e.getGuardFederation(clocks).intersect(targetFedAfterReset));
                    }


                    if (thereWasAnEdge) {
                        List<Edge> allEdges =edges.stream().filter(e -> e.getSource().equals(l) && e.getChannel().equals(c) && e.getTarget().equals(targetLoc)).collect(Collectors.toList());
                        Update[] updates = allEdges.get(0).getUpdates();
                        for (Edge e : allEdges)
                            assert(Arrays.equals(e.getUpdates(),(updates)));
                        finalEdges.add(new Edge(l, targetLoc, c,  allEdges.get(0).isInput(), allFeds.turnFederationToGuards(clocks), allEdges.get(0).getUpdates()));
                    }

                }
            }
        }



        return new Automaton(aut.getName()+"Bisimilar",locs,finalEdges,clocks);

    }

    private static int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.hashCode() == clocks.get(i).hashCode()) return i + 1;
        }
        return 0;
    }

    public static boolean hasDifferentZone(Location l1, Location l2, List<Clock> clocks)
    {
        if (Federation.fedEqFed(l1.getInvariantFederation(clocks), l2.getInvariantFederation(clocks))) {


            return false;
        }
        System.out.println("Feds not equal: " + l1.getName() + " "  + l2.getName());
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

        Federation s1 = l1.getInvariantFederation(clocks);
        Federation s2 = l2.getInvariantFederation(clocks);

        for (Edge e1 : edgesL1)
        {
            Channel c = e1.getChan();
            if (edgesL2.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()).isEmpty()) {
                System.out.println("chan not there 1 " + c + " " +  l1.getName() + " " + l2.getName());
                return true;
            }
            Federation e1Fed = s1.intersect(e1.getGuardFederation(clocks));
            Federation e2Fed = null;
            for (Edge e2 : edgesL2.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()))
            {
                if (e2.getChannel().equals(c))
                {
                    if (e2Fed==null)
                        e2Fed = s2.intersect(e2.getGuardFederation(clocks));
                    else
                        e2Fed = Federation.fedPlusFed(s2.intersect(e2.getGuardFederation(clocks)),e2Fed);
                }
                if (e1Fed.intersects(s2.intersect(e2.getGuardFederation(clocks))) && !Arrays.equals(e1.getUpdates(),e2.getUpdates()))
                {
                    System.out.println("updates not eqal 1 " + l1.getName() + " " + l2.getName());
                    return true;
                }


               if (e1Fed.intersects(s2.intersect(e2.getGuardFederation(clocks))) && getIndexInBislimlarLocs(e1.getTarget(), bisimilarLocs)!=getIndexInBislimlarLocs(e2.getTarget(),bisimilarLocs))
                {
                    if (l1.getName().equals(l2.getName())) {
                        System.out.println("same location: " + l1.getName() + " " + getIndexInBislimlarLocs(e1.getTarget(), bisimilarLocs) + "  " + getIndexInBislimlarLocs(e2.getTarget(), bisimilarLocs));
                        System.out.println(e1.getTarget().getName() + " " +  e2.getTarget().getName() + " " + e1.getChannel() + " " + e2.getChannel());
                        System.out.println(bisimilarLocs);
                    }
                    System.out.println("different target location lists 1");
                    return true;
                }
            }
            if (!e1Fed.isSubset(e2Fed)) {
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
                System.out.println("not the same channel 2 " + l1.getName() + " " + l2.getName());
                return true;
            }
            Federation e2Fed = s2.intersect(e2.getGuardFederation(clocks));
            Federation e1Fed = null;
            for (Edge e1 : edgesL1.stream().filter(e->e.getChannel().equals(c)).collect(Collectors.toList()))
            {
                if (e1.getChannel().equals(c))
                {
                    if (e1Fed==null)
                        e1Fed = s1.intersect(e1.getGuardFederation(clocks));
                    else
                        e1Fed = Federation.fedPlusFed(s1.intersect(e1.getGuardFederation(clocks)),e1Fed);
                }

                if (e2Fed.intersects(s1.intersect(e1.getGuardFederation(clocks))) && getIndexInBislimlarLocs(e1.getTarget(),bisimilarLocs)!=getIndexInBislimlarLocs(e2.getTarget(),bisimilarLocs))
                {
                    System.out.println("different target location lists 2");

                    return true;
                }


            }
            if (!e2Fed.isSubset(e1Fed)) {
                System.out.println("outfed not subset " + l1.getName() + " " + l2.getName());
                return true;
            }
        }


        System.out.println("no difference " + l1.getName() + " " + l2.getName());


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
