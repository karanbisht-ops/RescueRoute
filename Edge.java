package com.disasterops.model;

/**
 * Represents a road connecting two locations in the disaster-response road
 * network graph. Roads can be marked "blocked" at runtime — for example,
 * when a field volunteer reports flooding or debris — and the routing
 * engine ({@link com.disasterops.core.Graph}) will avoid blocked roads
 * when computing the shortest safe path.
 *
 * @author Team DisasterOps
 */
public class Edge {

    private final String to;
    private final double distanceKm;
    private boolean blocked;

    /**
     * @param to          the location this edge leads to
     * @param distanceKm  road distance in kilometers (must be &gt; 0)
     * @throws IllegalArgumentException if distanceKm is not positive
     */
    public Edge(String to, double distanceKm) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Edge destination cannot be empty.");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("distanceKm must be positive, got: " + distanceKm);
        }
        this.to = to;
        this.distanceKm = distanceKm;
        this.blocked = false;
    }

    public String getTo() { return to; }
    public double getDistanceKm() { return distanceKm; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
