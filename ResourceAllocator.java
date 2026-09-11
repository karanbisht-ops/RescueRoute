package com.disasterops.core;

import com.disasterops.model.Zone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Solves relief-resource allocation as a classic 0/1 Knapsack problem: given
 * a fixed supply capacity and a list of candidate zones (each needing a
 * certain number of units, and offering a certain number of people helped
 * if fully supplied), finds the subset of zones that maximizes total people
 * helped without exceeding capacity.
 *
 * <p><b>Time complexity:</b> {@code O(zones * capacity)} using bottom-up
 * dynamic programming.</p>
 *
 * @author Team DisasterOps
 */
public class ResourceAllocator {

    /** Immutable result of an allocation run. */
    public static final class AllocationResult {
        public final List<Zone> selectedZones;
        public final int totalUnitsUsed;
        public final int totalPeopleHelped;

        AllocationResult(List<Zone> selectedZones, int totalUnitsUsed, int totalPeopleHelped) {
            this.selectedZones = selectedZones;
            this.totalUnitsUsed = totalUnitsUsed;
            this.totalPeopleHelped = totalPeopleHelped;
        }
    }

    /**
     * Computes the optimal zone selection for the given supply capacity.
     *
     * @param zones     candidate zones requesting supply
     * @param capacity  total available supply units (must be &ge; 0)
     * @return the optimal {@link AllocationResult}
     * @throws IllegalArgumentException if zones is null or capacity is negative
     */
    public AllocationResult allocate(List<Zone> zones, int capacity) {
        if (zones == null) {
            throw new IllegalArgumentException("zones list cannot be null.");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative, got: " + capacity);
        }

        int n = zones.size();
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            Zone zone = zones.get(i - 1);
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i - 1][w]; // baseline: don't take this zone
                if (zone.getUnitsNeeded() <= w) {
                    dp[i][w] = Math.max(dp[i][w],
                            dp[i - 1][w - zone.getUnitsNeeded()] + zone.getPeopleHelped());
                }
            }
        }

        // Backtrack through the DP table to recover which zones were selected
        List<Zone> selected = new ArrayList<>();
        int remaining = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][remaining] != dp[i - 1][remaining]) {
                Zone zone = zones.get(i - 1);
                selected.add(zone);
                remaining -= zone.getUnitsNeeded();
            }
        }
        Collections.reverse(selected);

        int unitsUsed = selected.stream().mapToInt(Zone::getUnitsNeeded).sum();
        return new AllocationResult(selected, unitsUsed, dp[n][capacity]);
    }
}
