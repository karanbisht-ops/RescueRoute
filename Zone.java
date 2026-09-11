package com.disasterops.model;

/**
 * Represents an affected zone requesting relief supplies.
 *
 * <p>Used as an "item" in the 0/1 Knapsack formulation solved by
 * {@link com.disasterops.core.ResourceAllocator}: {@code unitsNeeded} is the
 * item's weight, and {@code peopleHelped} is its value.</p>
 *
 * @author Team DisasterOps
 */
public class Zone {

    private final String name;
    private final int unitsNeeded;
    private final int peopleHelped;

    /**
     * @param name          display name of the zone
     * @param unitsNeeded   supply units required to fully serve this zone (must be &gt; 0)
     * @param peopleHelped  number of people helped if this zone is fully supplied (must be &ge; 0)
     * @throws IllegalArgumentException if name is blank, or the numeric inputs are invalid
     */
    public Zone(String name, int unitsNeeded, int peopleHelped) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Zone name cannot be empty.");
        }
        if (unitsNeeded <= 0) {
            throw new IllegalArgumentException("unitsNeeded must be positive, got: " + unitsNeeded);
        }
        if (peopleHelped < 0) {
            throw new IllegalArgumentException("peopleHelped cannot be negative, got: " + peopleHelped);
        }
        this.name = name;
        this.unitsNeeded = unitsNeeded;
        this.peopleHelped = peopleHelped;
    }

    public String getName() { return name; }
    public int getUnitsNeeded() { return unitsNeeded; }
    public int getPeopleHelped() { return peopleHelped; }

    @Override
    public String toString() {
        return String.format("%-8s | Needs: %2d units | Helps: %3d people", name, unitsNeeded, peopleHelped);
    }
}
