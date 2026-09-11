package com.disasterops.core;

import com.disasterops.model.Victim;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Ranks incoming victim reports by urgency using {@link java.util.PriorityQueue}.
 * The ordering is defined by {@link Victim#compareTo(Victim)}: highest
 * severity first, then earliest arrival as a tie-break.
 *
 * <p><b>Time complexity:</b> {@code O(log n)} per report/dispatch operation.</p>
 *
 * @author Team DisasterOps
 */
public class TriageSystem {

    private final PriorityQueue<Victim> queue = new PriorityQueue<>();

    /** Adds a new victim report to the triage queue. */
    public void reportVictim(Victim victim) {
        if (victim == null) {
            throw new IllegalArgumentException("Cannot report a null victim.");
        }
        queue.add(victim);
    }

    /**
     * Removes and returns the most urgent victim currently waiting.
     *
     * @return the next victim to dispatch, or {@code null} if the queue is empty
     */
    public Victim dispatchNext() {
        return queue.poll();
    }

    public boolean hasPending() {
        return !queue.isEmpty();
    }

    public int pendingCount() {
        return queue.size();
    }

    /**
     * Returns the full dispatch order (most urgent first) without modifying
     * the live queue — useful for displaying a preview during a demo.
     */
    public List<Victim> previewDispatchOrder() {
        PriorityQueue<Victim> copy = new PriorityQueue<>(queue);
        List<Victim> ordered = new ArrayList<>();
        while (!copy.isEmpty()) {
            ordered.add(copy.poll());
        }
        return ordered;
    }
}
