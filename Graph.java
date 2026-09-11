package com.disasterops.core;

import com.disasterops.model.Edge;
import java.util.*;

/**
 * Models the disaster-response road network as a weighted, undirected graph
 * using an adjacency list, and computes shortest safe paths using
 * Dijkstra's Algorithm.
 *
 * <p>Roads can be marked blocked at runtime (e.g. reported by a field
 * volunteer); blocked roads are skipped entirely when computing routes,
 * so the "shortest path" returned is always a currently-safe path.</p>
 *
 * <p>Beyond single-destination routing, this class also supports:</p>
 * <ul>
 *   <li><b>Nearest-shelter routing</b> — given several candidate shelters,
 *       finds the closest one that is actually reachable (a multi-destination
 *       shortest-path problem, common in real emergency logistics where the
 *       nearest facility is not known in advance).</li>
 *   <li><b>Network resilience analysis</b> — using a multi-source Breadth-First
 *       Search from all shelters simultaneously, identifies which locations
 *       become completely cut off (unreachable from every shelter) once
 *       enough roads are blocked. This uses a different algorithm family
 *       (unweighted graph traversal) from the weighted shortest-path work
 *       Dijkstra does, and answers a genuinely different question: not
 *       "what is the best route" but "is a route possible at all".</li>
 * </ul>
 *
 * <p><b>Time complexity:</b> {@code O((V + E) log V)} per Dijkstra-based
 * shortest-path query; {@code O(V + E)} per resilience/reachability check.</p>
 *
 * @author Team DisasterOps
 */
public class Graph {

    private final Map<String, List<Edge>> adjacency = new LinkedHashMap<>();

    /** Registers a location in the network (a no-op if it already exists). */
    public void addLocation(String name) {
        adjacency.putIfAbsent(name, new ArrayList<>());
    }

    /**
     * Adds a two-way road between two locations.
     *
     * @param from        first location
     * @param to          second location
     * @param distanceKm  road distance in kilometers
     */
    public void addRoad(String from, String to, double distanceKm) {
        addLocation(from);
        addLocation(to);
        adjacency.get(from).add(new Edge(to, distanceKm));
        adjacency.get(to).add(new Edge(from, distanceKm));
    }

    /**
     * Marks the road between two locations as blocked or clear, in both directions.
     *
     * @return true if a matching road was found and updated, false otherwise
     */
    public boolean setRoadBlocked(String from, String to, boolean blocked) {
        boolean found = false;
        for (Edge e : adjacency.getOrDefault(from, List.of())) {
            if (e.getTo().equals(to)) { e.setBlocked(blocked); found = true; }
        }
        for (Edge e : adjacency.getOrDefault(to, List.of())) {
            if (e.getTo().equals(from)) { e.setBlocked(blocked); found = true; }
        }
        return found;
    }

    /** Returns an unmodifiable view of all known location names. */
    public Set<String> getLocations() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /** Returns true if the given location has been registered in the graph. */
    public boolean hasLocation(String name) {
        return adjacency.containsKey(name);
    }

    // ---------------------------------------------------------------
    // Dijkstra's Algorithm (shared core)
    // ---------------------------------------------------------------

    /**
     * Runs Dijkstra's Algorithm once from {@code start}, computing the
     * shortest safe distance to every other reachable location. Both
     * {@link #shortestSafePath} and {@link #nearestReachableShelter} reuse
     * this single-source computation rather than duplicating the algorithm.
     */
    private DijkstraResult runDijkstra(String start) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String node : adjacency.keySet()) dist.put(node, Double.POSITIVE_INFINITY);

        if (!adjacency.containsKey(start)) {
            return new DijkstraResult(dist, prev);
        }
        dist.put(start, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(start);
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (!visited.add(current)) continue;

            for (Edge edge : adjacency.get(current)) {
                if (edge.isBlocked()) continue; // never route through an unsafe road
                double candidate = dist.get(current) + edge.getDistanceKm();
                if (candidate < dist.get(edge.getTo())) {
                    dist.put(edge.getTo(), candidate);
                    prev.put(edge.getTo(), current);
                    pq.add(edge.getTo());
                }
            }
        }
        return new DijkstraResult(dist, prev);
    }

    private PathResult buildPathResult(DijkstraResult dr, String start, String end) {
        double distance = dr.dist.getOrDefault(end, Double.POSITIVE_INFINITY);
        if (distance == Double.POSITIVE_INFINITY) {
            return PathResult.unreachable();
        }
        LinkedList<String> path = new LinkedList<>();
        for (String step = end; step != null; step = dr.prev.get(step)) {
            path.addFirst(step);
        }
        return new PathResult(path, distance, true);
    }

    /**
     * Computes the shortest currently-safe path between two specific locations.
     *
     * @param start starting location
     * @param end   destination location
     * @return a {@link PathResult} describing the path found, or an
     *         unreachable result if no safe path exists
     */
    public PathResult shortestSafePath(String start, String end) {
        if (!adjacency.containsKey(start) || !adjacency.containsKey(end)) {
            return PathResult.unreachable();
        }
        DijkstraResult dr = runDijkstra(start);
        return buildPathResult(dr, start, end);
    }

    /**
     * Multi-destination routing: given a list of candidate shelters, finds
     * the one that is closest to {@code start} by actual safe road distance
     * (not straight-line distance), skipping any shelter that is currently
     * unreachable. This mirrors a real nearest-facility problem, where
     * responders do not know in advance which shelter will end up closest
     * once some roads are blocked.
     *
     * @param start          starting location
     * @param shelterNames   candidate shelter locations to consider
     * @return the {@link PathResult} to the nearest reachable shelter, or an
     *         unreachable result if none of the shelters can currently be reached
     */
    public PathResult nearestReachableShelter(String start, List<String> shelterNames) {
        if (!adjacency.containsKey(start)) {
            return PathResult.unreachable();
        }
        DijkstraResult dr = runDijkstra(start);

        String bestShelter = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (String shelter : shelterNames) {
            double d = dr.dist.getOrDefault(shelter, Double.POSITIVE_INFINITY);
            if (d < bestDistance) {
                bestDistance = d;
                bestShelter = shelter;
            }
        }
        if (bestShelter == null) {
            return PathResult.unreachable();
        }
        return buildPathResult(dr, start, bestShelter);
    }

    // ---------------------------------------------------------------
    // Network Resilience Analysis (multi-source BFS)
    // ---------------------------------------------------------------

    /**
     * Identifies every location that is completely cut off from all relief
     * shelters given the roads currently marked blocked. Uses a
     * multi-source Breadth-First Search starting simultaneously from every
     * shelter — deliberately a different algorithm from Dijkstra, since this
     * question ("is any safe route possible at all") does not need road
     * distances, only reachability.
     *
     * @param shelterNames  all known shelter locations
     * @return the set of locations with no safe path to any shelter
     */
    public Set<String> findIsolatedLocations(List<String> shelterNames) {
        Set<String> reachable = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();

        for (String shelter : shelterNames) {
            if (adjacency.containsKey(shelter) && reachable.add(shelter)) {
                queue.add(shelter);
            }
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Edge edge : adjacency.get(current)) {
                if (edge.isBlocked()) continue;
                if (reachable.add(edge.getTo())) {
                    queue.add(edge.getTo());
                }
            }
        }

        Set<String> isolated = new LinkedHashSet<>(adjacency.keySet());
        isolated.removeAll(reachable);
        return isolated;
    }

    // ---------------------------------------------------------------
    // Result types
    // ---------------------------------------------------------------

    /** Immutable result of a shortest-path query. */
    public static final class PathResult {
        public final List<String> path;
        public final double totalDistanceKm;
        public final boolean reachable;

        public PathResult(List<String> path, double totalDistanceKm, boolean reachable) {
            this.path = path;
            this.totalDistanceKm = totalDistanceKm;
            this.reachable = reachable;
        }

        static PathResult unreachable() {
            return new PathResult(Collections.emptyList(), Double.POSITIVE_INFINITY, false);
        }
    }

    /** Internal holder for a single-source Dijkstra run's raw results. */
    private static final class DijkstraResult {
        final Map<String, Double> dist;
        final Map<String, String> prev;

        DijkstraResult(Map<String, Double> dist, Map<String, String> prev) {
            this.dist = dist;
            this.prev = prev;
        }
    }
}

