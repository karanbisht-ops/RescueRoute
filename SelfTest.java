package com.disasterops;

import com.disasterops.core.Graph;
import com.disasterops.core.ResourceAllocator;
import com.disasterops.core.TriageSystem;
import com.disasterops.model.Victim;
import com.disasterops.model.Zone;

import java.util.List;
import java.util.Set;

/**
 * Lightweight, dependency-free self-test runner. Run this file's main()
 * to automatically verify that all three modules produce correct results
 * against known expected values, without needing JUnit or any external
 * testing framework.
 *
 * <p>Useful both as a safety net before a live demo, and as evidence of
 * testing discipline when presenting the source code to judges.</p>
 *
 * @author Team DisasterOps
 */
public class SelfTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Running DisasterOps self-tests...\n");

        testTriageOrdering();
        testTriageTieBreakOnEqualSeverity();
        testDijkstraShortestPath();
        testDijkstraReroutesAroundBlockedRoad();
        testKnapsackOptimalAllocation();
        testNearestReachableShelterPicksClosest();
        testNearestReachableShelterSkipsUnreachableOne();
        testNetworkResilienceDetectsIsolatedLocation();
        testNetworkResilienceReportsNoneWhenConnected();

        System.out.println("\n---------------------------------------------");
        System.out.printf("RESULT: %d passed, %d failed%n", passed, failed);
        System.out.println(failed == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED");
        System.out.println("---------------------------------------------");
    }

    private static void testTriageOrdering() {
        TriageSystem t = new TriageSystem();
        // Low severity, long wait, close by
        t.reportVictim(new Victim("Low severity case", 2, 40, 3.0, 1, "Zone1"));
        // High severity, short wait, farther away
        t.reportVictim(new Victim("High severity case", 5, 5, 6.0, 2, "Zone2"));

        Victim first = t.dispatchNext();
        check("Triage: higher severity dispatched first despite shorter wait / farther distance",
                first.getSeverity() == 5);
    }

    private static void testTriageTieBreakOnEqualSeverity() {
        TriageSystem t = new TriageSystem();
        // Both severity 5, but different wait time and proximity should break the tie
        t.reportVictim(new Victim("Same severity, shorter wait, farther", 5, 5, 6.0, 1, "Zone1"));
        t.reportVictim(new Victim("Same severity, longer wait, closer", 5, 20, 2.2, 2, "Zone2"));

        Victim first = t.dispatchNext();
        check("Triage: among equal severity, higher combined wait+proximity score goes first",
                first.getName().equals("Same severity, longer wait, closer"));
    }

    private static void testDijkstraShortestPath() {
        Graph g = new Graph();
        g.addRoad("A", "B", 5.0);
        g.addRoad("B", "C", 5.0);
        g.addRoad("A", "C", 20.0); // longer direct route

        Graph.PathResult result = g.shortestSafePath("A", "C");
        check("Dijkstra: picks shorter A-B-C route over direct A-C",
                result.reachable && Math.abs(result.totalDistanceKm - 10.0) < 0.001);
    }

    private static void testDijkstraReroutesAroundBlockedRoad() {
        Graph g = new Graph();
        g.addRoad("A", "B", 5.0);
        g.addRoad("B", "C", 5.0);
        g.addRoad("A", "C", 20.0);

        g.setRoadBlocked("A", "B", true);
        Graph.PathResult result = g.shortestSafePath("A", "C");
        check("Dijkstra: reroutes via direct road when shorter path is blocked",
                result.reachable && Math.abs(result.totalDistanceKm - 20.0) < 0.001);
    }

    private static void testKnapsackOptimalAllocation() {
        List<Zone> zones = List.of(
                new Zone("A", 15, 80),
                new Zone("B", 10, 60),
                new Zone("C", 20, 95),
                new Zone("D", 8, 45)
        );
        ResourceAllocator.AllocationResult result = new ResourceAllocator().allocate(zones, 40);

        // Brute-force verified optimum for this exact dataset: Zone B + C + D = 38 units, 200 people
        check("Knapsack: finds true optimum (200 people, not a suboptimal combination)",
                result.totalPeopleHelped == 200 && result.totalUnitsUsed == 38);
    }

    private static void testNearestReachableShelterPicksClosest() {
        Graph g = new Graph();
        g.addRoad("Start", "ShelterNear", 4.0);
        g.addRoad("Start", "ShelterFar", 12.0);

        Graph.PathResult result = g.nearestReachableShelter("Start", List.of("ShelterNear", "ShelterFar"));
        check("Nearest-shelter routing: picks the genuinely closer of two candidate shelters",
                result.reachable && result.path.get(result.path.size() - 1).equals("ShelterNear")
                        && Math.abs(result.totalDistanceKm - 4.0) < 0.001);
    }

    private static void testNearestReachableShelterSkipsUnreachableOne() {
        Graph g = new Graph();
        g.addRoad("Start", "ShelterNear", 4.0);
        g.addRoad("Start", "ShelterFar", 12.0);
        g.setRoadBlocked("Start", "ShelterNear", true); // block the closer shelter's only road

        Graph.PathResult result = g.nearestReachableShelter("Start", List.of("ShelterNear", "ShelterFar"));
        check("Nearest-shelter routing: automatically falls back to the farther shelter when the nearer one is unreachable",
                result.reachable && result.path.get(result.path.size() - 1).equals("ShelterFar"));
    }

    private static void testNetworkResilienceDetectsIsolatedLocation() {
        Graph g = new Graph();
        g.addRoad("Village", "Junction", 3.0);
        g.addRoad("Junction", "Shelter", 3.0);

        g.setRoadBlocked("Village", "Junction", true); // Village's only connection to the network

        Set<String> isolated = g.findIsolatedLocations(List.of("Shelter"));
        check("Network resilience: correctly flags a village as isolated once its only road is blocked",
                isolated.contains("Village") && !isolated.contains("Junction"));
    }

    private static void testNetworkResilienceReportsNoneWhenConnected() {
        Graph g = new Graph();
        g.addRoad("Village", "Junction", 3.0);
        g.addRoad("Junction", "Shelter", 3.0);
        // no roads blocked here

        Set<String> isolated = g.findIsolatedLocations(List.of("Shelter"));
        check("Network resilience: reports zero isolated locations when the network is fully connected",
                isolated.isEmpty());
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
            passed++;
        } else {
            System.out.println("[FAIL] " + description);
            failed++;
        }
    }
}
