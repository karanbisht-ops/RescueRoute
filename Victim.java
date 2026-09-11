package com.disasterops.model;

/**
 * Represents a single victim / emergency report received during a disaster.
 *
 * <p>Implements {@link Comparable} so that instances can be placed directly
 * into a {@link java.util.PriorityQueue}. Priority is computed as:</p>
 *
 * <pre>priority = (severity * 10) + (waitTimeMinutes / 60) - (proximityKm / 1000)</pre>
 *
 * <p>Higher severity dominates the score (it is weighted most heavily,
 * matching real-world triage practice where life-threatening cases must
 * always be surfaced first). Among victims of equal severity, longer
 * wait time increases priority (so long-waiting victims are not
 * neglected) and greater distance from the nearest hospital slightly
 * lowers priority (a logistical feasibility factor). If two scores are
 * exactly equal, earlier arrival order is used as the final tie-break.</p>
 *
 * @author Team DisasterOps
 */
public class Victim implements Comparable<Victim> {

    private static final int MIN_SEVERITY = 1;
    private static final int MAX_SEVERITY = 5;

    private final String name;
    private final int severity;
    private final double waitTimeMinutes;
    private final double proximityKm;
    private final int arrivalOrder;
    private final String location;

    /**
     * Creates a new victim report.
     *
     * @param name             display name of the victim
     * @param severity         urgency level, 1 (minor) to 5 (critical)
     * @param waitTimeMinutes  minutes already spent waiting for a response
     * @param proximityKm      distance in km to the nearest hospital/shelter
     * @param arrivalOrder     sequence number indicating when the report came in
     *                         (lower means earlier) — used only as a final tie-break
     * @param location         human-readable location of the victim
     * @throws IllegalArgumentException if severity is outside 1-5,
     *                                   waitTime/proximity are negative,
     *                                   or name/location is null or blank
     */
    public Victim(String name, int severity, double waitTimeMinutes, double proximityKm,
                  int arrivalOrder, String location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Victim name cannot be empty.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Victim location cannot be empty.");
        }
        if (severity < MIN_SEVERITY || severity > MAX_SEVERITY) {
            throw new IllegalArgumentException(
                    "Severity must be between " + MIN_SEVERITY + " and " + MAX_SEVERITY
                            + ", got: " + severity);
        }
        if (waitTimeMinutes < 0 || proximityKm < 0) {
            throw new IllegalArgumentException("waitTimeMinutes and proximityKm cannot be negative.");
        }
        this.name = name;
        this.severity = severity;
        this.waitTimeMinutes = waitTimeMinutes;
        this.proximityKm = proximityKm;
        this.arrivalOrder = arrivalOrder;
        this.location = location;
    }

    public String getName() { return name; }
    public int getSeverity() { return severity; }
    public double getWaitTimeMinutes() { return waitTimeMinutes; }
    public double getProximityKm() { return proximityKm; }
    public int getArrivalOrder() { return arrivalOrder; }
    public String getLocation() { return location; }

    /**
     * Computes this victim's priority score using the formula:
     * {@code (severity * 10) + (waitTimeMinutes / 60) - (proximityKm / 1000)}.
     * Higher scores are dispatched first.
     */
    public double getPriorityScore() {
        return (severity * 10.0) + (waitTimeMinutes / 60.0) - (proximityKm / 1000.0);
    }

    /**
     * Defines the priority ordering used by the triage queue: higher
     * priority score wins; ties are broken by earlier arrival order.
     */
    @Override
    public int compareTo(Victim other) {
        int scoreComparison = Double.compare(other.getPriorityScore(), this.getPriorityScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        return this.arrivalOrder - other.arrivalOrder;
    }

    @Override
    public String toString() {
        return String.format("%-16s | Severity: %d | Wait: %.0fm | Dist: %.1fkm | Score: %.2f | %s",
                name, severity, waitTimeMinutes, proximityKm, getPriorityScore(), location);
    }
}

