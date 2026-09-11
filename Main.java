package com.disasterops;

import com.disasterops.core.Graph;
import com.disasterops.core.ResourceAllocator;
import com.disasterops.core.TriageSystem;
import com.disasterops.model.Victim;
import com.disasterops.model.Zone;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * DisasterOps — Offline Disaster Response Decision-Support System.
 * GBU Internal Hackathon for Smart India Hackathon 2026 | Theme: Disaster Management
 *
 * <p>Entry point and menu controller. Case-study data lives in
 * {@link DemoData} and {@link Scenario}; the core algorithms live in the
 * {@code com.disasterops.core} package. This class only wires them together
 * and handles console I/O.</p>
 *
 * <p>Run this file's {@code main()}, pick one of three disaster case studies,
 * then choose option 5 for a guided full walkthrough covering all four
 * modules in order.</p>
 *
 * @author Team DisasterOps
 */
public class Main {

    private static final String DIVIDER = "-----------------------------------------------------";

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            printBanner();
            List<Scenario> scenarios = DemoData.allScenarios();

            boolean keepRunning = true;
            while (keepRunning) {
                Scenario scenario = chooseScenario(sc, scenarios);
                keepRunning = runScenarioMenu(sc, scenario);
            }
        }
        System.out.println("\nExiting DisasterOps. Stay safe.\n");
    }

    // ---------------------------------------------------------------
    // Scenario selection
    // ---------------------------------------------------------------

    private static Scenario chooseScenario(Scanner sc, List<Scenario> scenarios) {
        System.out.println(DIVIDER);
        System.out.println("CHOOSE A CASE STUDY");
        System.out.println(DIVIDER);
        for (int i = 0; i < scenarios.size(); i++) {
            Scenario s = scenarios.get(i);
            System.out.printf("%d. %s%n   %s%n", i + 1, s.getName(), s.getDescription());
        }
        System.out.print("Enter choice (1-" + scenarios.size() + "): ");
        String input = sc.hasNextLine() ? sc.nextLine().trim() : "1";
        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            index = 0;
        }
        if (index < 0 || index >= scenarios.size()) {
            index = 0;
        }
        Scenario chosen = scenarios.get(index);
        System.out.println("\nLoaded case study: " + chosen.getName() + "\n");
        return chosen;
    }

    // ---------------------------------------------------------------
    // Per-scenario menu
    // ---------------------------------------------------------------

    /** @return true if the user wants to pick a different case study, false to exit. */
    private static boolean runScenarioMenu(Scanner sc, Scenario scenario) {
        while (true) {
            printMenu(scenario);
            System.out.print("Enter choice: ");
            String choice = sc.hasNextLine() ? sc.nextLine().trim() : "6";

            switch (choice) {
                case "1" -> runTriageDemo(scenario.getTriageSystem());
                case "2" -> runRoutingDemo(sc, scenario, true);
                case "3" -> runAllocationDemo(scenario);
                case "4" -> runResilienceDemo(scenario);
                case "5" -> runFullWalkthrough(scenario);
                case "6" -> { return true; }  // switch case study
                case "7" -> { return false; } // exit program
                default -> System.out.println("Invalid choice - please enter a number from 1 to 7.\n");
            }
        }
    }

    // ---------------------------------------------------------------
    // Module 1: Triage
    // ---------------------------------------------------------------

    private static void runTriageDemo(TriageSystem triage) {
        System.out.println("\n" + DIVIDER);
        System.out.println("MODULE 1: TRIAGE PRIORITIZATION");
        System.out.println(DIVIDER);
        System.out.println("Formula: priority = (severity x 10) + (waitTime / 60) - (proximity / 1000)");
        System.out.println("Data structure: java.util.PriorityQueue<Victim> with a custom Comparable.\n");
        System.out.println("Dispatch order (most urgent first):");

        List<Victim> order = triage.previewDispatchOrder();
        for (int i = 0; i < order.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, order.get(i));
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // Module 2: Multi-shelter routing
    // ---------------------------------------------------------------

    private static Graph.PathResult runRoutingDemo(Scanner sc, Scenario scenario, boolean interactive) {
        System.out.println("\n" + DIVIDER);
        System.out.println("MODULE 2: NEAREST-SHELTER ROUTING (MULTI-DESTINATION)");
        System.out.println(DIVIDER);
        System.out.println("Algorithm: Graph (adjacency list) + Dijkstra's Algorithm, run once from the");
        System.out.println("start location, then compared against every candidate shelter.\n");

        Graph graph = scenario.getGraph();
        String start = scenario.getStartLocation();
        List<String> shelters = scenario.getShelterNames();

        System.out.println("Candidate shelters considered: " + String.join(", ", shelters));
        Graph.PathResult latestResult = printNearestShelter(graph, start, shelters);

        boolean simulateBlock;
        if (interactive) {
            System.out.print("\nSimulate a blocked road? (y/n): ");
            String answer = sc.hasNextLine() ? sc.nextLine().trim() : "n";
            simulateBlock = answer.equalsIgnoreCase("y");
        } else {
            simulateBlock = true; // always demonstrate rerouting in the full walkthrough
        }

        if (simulateBlock) {
            String from = scenario.getDemoBlockedRoadFrom();
            String to = scenario.getDemoBlockedRoadTo();
            graph.setRoadBlocked(from, to, true);
            System.out.printf("%n[Volunteer Report] Road '%s <-> %s' marked BLOCKED.%n", from, to);
            System.out.println("Recalculating nearest reachable shelter across all candidates...\n");
            latestResult = printNearestShelter(graph, start, shelters);
            graph.setRoadBlocked(from, to, false); // reset for repeat demo runs
        }
        System.out.println();
        return latestResult;
    }

    private static Graph.PathResult printNearestShelter(Graph graph, String start, List<String> shelters) {
        Graph.PathResult result = graph.nearestReachableShelter(start, shelters);
        if (!result.reachable) {
            System.out.printf("No shelter is currently reachable from '%s'.%n", start);
            return result;
        }
        String destination = result.path.get(result.path.size() - 1);
        System.out.printf("Nearest reachable shelter from '%s': %s%n", start, destination);
        System.out.println("  Route: " + String.join("  ->  ", result.path));
        System.out.printf("  Total distance: %.1f km%n", result.totalDistanceKm);
        return result;
    }

    // ---------------------------------------------------------------
    // Module 3: Resource allocation
    // ---------------------------------------------------------------

    private static ResourceAllocator.AllocationResult runAllocationDemo(Scenario scenario) {
        System.out.println("\n" + DIVIDER);
        System.out.println("MODULE 3: RESOURCE ALLOCATION OPTIMIZER");
        System.out.println(DIVIDER);
        System.out.println("Algorithm: 0/1 Knapsack (Dynamic Programming).\n");

        List<Zone> zones = scenario.getZones();
        int capacity = scenario.getSupplyCapacity();

        System.out.println("Available supply capacity: " + capacity + " units");
        System.out.println("Zones requesting supply:");
        zones.forEach(z -> System.out.println("  " + z));

        ResourceAllocator.AllocationResult result = new ResourceAllocator().allocate(zones, capacity);

        System.out.println("\nOptimal allocation:");
        result.selectedZones.forEach(z -> System.out.println("  -> " + z.getName()));
        System.out.printf("  Total units used: %d / %d%n", result.totalUnitsUsed, capacity);
        System.out.printf("  Total people helped: %d (maximum possible)%n%n", result.totalPeopleHelped);
        return result;
    }

    // ---------------------------------------------------------------
    // Module 4: Network resilience (new — multi-source BFS)
    // ---------------------------------------------------------------

    private static Set<String> runResilienceDemo(Scenario scenario) {
        System.out.println("\n" + DIVIDER);
        System.out.println("MODULE 4: NETWORK RESILIENCE CHECK");
        System.out.println(DIVIDER);
        System.out.println("Algorithm: Multi-source Breadth-First Search (BFS) from every shelter at once.");
        System.out.println("Different from Dijkstra: this answers 'is any route possible', not 'which is shortest'.\n");

        Graph graph = scenario.getGraph();
        List<String[]> roadsToBlock = scenario.getIsolationDemoRoads();

        System.out.println("Simulating multiple road failures around: " + scenario.getIsolationDemoLocation());
        for (String[] road : roadsToBlock) {
            graph.setRoadBlocked(road[0], road[1], true);
            System.out.printf("  [Blocked] %s <-> %s%n", road[0], road[1]);
        }

        Set<String> isolated = graph.findIsolatedLocations(scenario.getShelterNames());
        System.out.println();
        if (isolated.isEmpty()) {
            System.out.println("Result: every location still has a safe path to at least one shelter.");
        } else {
            System.out.println("Result: the following locations are now completely cut off from every shelter:");
            isolated.forEach(loc -> System.out.println("  -> " + loc));
        }

        // Reset roads so repeat demo runs start clean
        for (String[] road : roadsToBlock) {
            graph.setRoadBlocked(road[0], road[1], false);
        }
        System.out.println();
        return isolated;
    }

    // ---------------------------------------------------------------
    // Full walkthrough (recommended for live demos)
    // ---------------------------------------------------------------

    private static void runFullWalkthrough(Scenario scenario) {
        System.out.println("\n=======================================================");
        System.out.println("   FULL CASE-STUDY WALKTHROUGH: " + scenario.getName());
        System.out.println("=======================================================");
        runTriageDemo(scenario.getTriageSystem());
        Graph.PathResult routeResult = runRoutingDemo(null, scenario, false);
        ResourceAllocator.AllocationResult allocationResult = runAllocationDemo(scenario);
        Set<String> isolated = runResilienceDemo(scenario);
        printImpactSummary(scenario, routeResult, allocationResult, isolated);
        System.out.println("=======================================================\n");
    }

    /**
     * Prints a one-screen summary tying together the results of all four
     * modules — useful as a closing beat in a live demo.
     */
    private static void printImpactSummary(Scenario scenario, Graph.PathResult routeResult,
                                            ResourceAllocator.AllocationResult allocationResult,
                                            Set<String> isolated) {
        System.out.println("\n" + DIVIDER);
        System.out.println("IMPACT SUMMARY: " + scenario.getName());
        System.out.println(DIVIDER);
        System.out.printf("Victims triaged and ranked by urgency: %d%n",
                scenario.getTriageSystem().pendingCount());
        if (routeResult != null && routeResult.reachable) {
            System.out.printf("Nearest reachable shelter found: %.1f km (recalculated live after a road block)%n",
                    routeResult.totalDistanceKm);
        } else {
            System.out.println("Nearest reachable shelter found: none available for the simulated scenario");
        }
        System.out.printf("Relief zones supplied: %d, helping %d people within a %d-unit capacity%n",
                allocationResult.selectedZones.size(), allocationResult.totalPeopleHelped,
                scenario.getSupplyCapacity());
        System.out.printf("Locations isolated under multi-road failure: %d%n", isolated.size());
        System.out.println(DIVIDER);
    }

    // ---------------------------------------------------------------
    // Console UI helpers
    // ---------------------------------------------------------------

    private static void printBanner() {
        System.out.println("=======================================================");
        System.out.println("   DisasterOps - Offline Disaster Response System");
        System.out.println("   GBU Internal Hackathon for SIH 2026");
        System.out.println("=======================================================\n");
    }

    private static void printMenu(Scenario scenario) {
        System.out.println(DIVIDER);
        System.out.println("Case Study: " + scenario.getName());
        System.out.println(DIVIDER);
        System.out.println("1. Run Triage Demo (Module 1)");
        System.out.println("2. Run Nearest-Shelter Routing Demo (Module 2)");
        System.out.println("3. Run Resource Allocation Demo (Module 3)");
        System.out.println("4. Run Network Resilience Check (Module 4)");
        System.out.println("5. Run Full Case-Study Walkthrough (all 4, in order)");
        System.out.println("6. Switch Case Study");
        System.out.println("7. Exit");
    }
}
