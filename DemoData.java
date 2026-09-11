package com.disasterops;

import com.disasterops.core.Graph;
import com.disasterops.core.TriageSystem;
import com.disasterops.model.Victim;
import com.disasterops.model.Zone;
import java.util.List;

/**
 * Builds the three disaster case studies used by the console demo. Each
 * scenario is a self-contained {@link Scenario} — its own road network,
 * victim reports, and relief zones — so the same algorithm code (Graph,
 * TriageSystem, ResourceAllocator) can be exercised against genuinely
 * different real-world situations without any changes to the core logic.
 *
 * @author Team DisasterOps
 */
public final class DemoData {

    private DemoData() { /* utility class, not instantiable */ }

    // ---------------------------------------------------------------
    // Case Study 1: Flood — GBU / Greater Noida
    // ---------------------------------------------------------------
    public static Scenario buildFloodScenario() {
        Graph g = new Graph();
        g.addRoad("GBU Campus", "Pari Chowk", 3.0);
        g.addRoad("GBU Campus", "Knowledge Park II", 2.2);
        g.addRoad("Pari Chowk", "Alpha 1 Sector", 4.5);
        g.addRoad("Pari Chowk", "Surajpur", 5.0);
        g.addRoad("Knowledge Park II", "Alpha 1 Sector", 3.8);
        g.addRoad("Alpha 1 Sector", "Relief Shelter - Sector Gamma", 2.0);
        g.addRoad("Surajpur", "Relief Shelter - Sector Gamma", 6.5);
        g.addRoad("Surajpur", "Relief Shelter - Sector Omega", 3.2);
        g.addRoad("Knowledge Park II", "Relief Shelter - Sector Omega", 7.0);

        TriageSystem t = new TriageSystem();
        // name, severity, waitTimeMinutes, proximityKm, arrivalOrder, location
        t.reportVictim(new Victim("Ramesh Kumar", 3, 15, 4.5, 1, "Alpha 1 Sector"));
        t.reportVictim(new Victim("Sunita Devi", 5, 5, 6.0, 2, "Surajpur"));
        t.reportVictim(new Victim("Aditya Rao", 2, 40, 3.0, 3, "Pari Chowk"));
        t.reportVictim(new Victim("Fatima Sheikh", 5, 20, 2.2, 4, "Knowledge Park II"));
        t.reportVictim(new Victim("Vikram Singh", 4, 10, 1.5, 5, "GBU Campus"));

        List<Zone> zones = List.of(
                new Zone("Zone A", 15, 80),
                new Zone("Zone B", 10, 60),
                new Zone("Zone C", 20, 95),
                new Zone("Zone D", 8, 45)
        );

        return new Scenario(
                "Flood - GBU / Greater Noida",
                "Monsoon flash flood along the Hindon river basin cuts off low-lying sectors near GBU.",
                g, t, zones, 40,
                "GBU Campus",
                List.of("Relief Shelter - Sector Gamma", "Relief Shelter - Sector Omega"),
                "Alpha 1 Sector", "Relief Shelter - Sector Gamma",
                "Surajpur",
                List.of(
                        new String[]{"Pari Chowk", "Surajpur"},
                        new String[]{"Surajpur", "Relief Shelter - Sector Gamma"},
                        new String[]{"Surajpur", "Relief Shelter - Sector Omega"}
                )
        );
    }

    // ---------------------------------------------------------------
    // Case Study 2: Earthquake — Dehradun, Uttarakhand
    // ---------------------------------------------------------------
    public static Scenario buildEarthquakeScenario() {
        Graph g = new Graph();
        g.addRoad("Clock Tower", "Rajpur Road", 2.5);
        g.addRoad("Clock Tower", "ISBT Dehradun", 6.0);
        g.addRoad("Rajpur Road", "Premnagar", 4.0);
        g.addRoad("Rajpur Road", "Relief Shelter - Doon Stadium", 3.5);
        g.addRoad("Premnagar", "Relief Shelter - Doon Stadium", 5.5);
        g.addRoad("ISBT Dehradun", "Premnagar", 7.0);
        g.addRoad("ISBT Dehradun", "Relief Shelter - IT Park Ground", 4.2);
        g.addRoad("Premnagar", "Relief Shelter - IT Park Ground", 6.8);

        TriageSystem t = new TriageSystem();
        t.reportVictim(new Victim("Deepak Rawat", 5, 8, 2.0, 1, "Rajpur Road"));
        t.reportVictim(new Victim("Meena Bisht", 4, 25, 5.5, 2, "Premnagar"));
        t.reportVictim(new Victim("Suresh Nautiyal", 2, 50, 6.2, 3, "ISBT Dehradun"));
        t.reportVictim(new Victim("Kavita Rana", 5, 12, 1.0, 4, "Clock Tower"));
        t.reportVictim(new Victim("Anil Negi", 3, 30, 4.0, 5, "Premnagar"));
        t.reportVictim(new Victim("Poonam Thapa", 4, 18, 3.2, 6, "Rajpur Road"));

        List<Zone> zones = List.of(
                new Zone("Rajpur Cluster", 12, 70),
                new Zone("Premnagar Cluster", 18, 90),
                new Zone("ISBT Cluster", 9, 50),
                new Zone("Clock Tower Cluster", 6, 40)
        );

        return new Scenario(
                "Earthquake - Dehradun, Uttarakhand",
                "A moderate earthquake damages roads and structures across central Dehradun.",
                g, t, zones, 35,
                "Clock Tower",
                List.of("Relief Shelter - Doon Stadium", "Relief Shelter - IT Park Ground"),
                "Rajpur Road", "Relief Shelter - Doon Stadium",
                "ISBT Dehradun",
                List.of(
                        new String[]{"Clock Tower", "ISBT Dehradun"},
                        new String[]{"ISBT Dehradun", "Premnagar"},
                        new String[]{"ISBT Dehradun", "Relief Shelter - IT Park Ground"}
                )
        );
    }

    // ---------------------------------------------------------------
    // Case Study 3: Cyclone — Puri, Odisha (coastal)
    // ---------------------------------------------------------------
    public static Scenario buildCycloneScenario() {
        Graph g = new Graph();
        g.addRoad("Puri Town Center", "Konark Road Junction", 5.0);
        g.addRoad("Puri Town Center", "Chandrabhaga", 8.5);
        g.addRoad("Konark Road Junction", "Satpada", 6.0);
        g.addRoad("Konark Road Junction", "Relief Shelter - Puri Stadium", 3.0);
        g.addRoad("Chandrabhaga", "Relief Shelter - Puri Stadium", 9.0);
        g.addRoad("Chandrabhaga", "Relief Shelter - Community Hall", 4.5);
        g.addRoad("Satpada", "Relief Shelter - Community Hall", 7.5);

        TriageSystem t = new TriageSystem();
        t.reportVictim(new Victim("Bijay Pradhan", 5, 6, 3.0, 1, "Chandrabhaga"));
        t.reportVictim(new Victim("Sabita Jena", 3, 35, 5.0, 2, "Puri Town Center"));
        t.reportVictim(new Victim("Rakesh Behera", 4, 15, 2.5, 3, "Konark Road Junction"));
        t.reportVictim(new Victim("Lakshmi Nayak", 5, 10, 6.5, 4, "Satpada"));
        t.reportVictim(new Victim("Sunil Mohanty", 2, 45, 4.0, 5, "Puri Town Center"));

        List<Zone> zones = List.of(
                new Zone("Coastal Zone 1", 14, 75),
                new Zone("Coastal Zone 2", 11, 65),
                new Zone("Inland Zone", 7, 42)
        );

        return new Scenario(
                "Cyclone - Puri, Odisha",
                "A cyclone makes landfall near Puri; coastal roads flood and fishing communities need evacuation.",
                g, t, zones, 30,
                "Puri Town Center",
                List.of("Relief Shelter - Puri Stadium", "Relief Shelter - Community Hall"),
                "Konark Road Junction", "Relief Shelter - Puri Stadium",
                "Satpada",
                List.of(
                        new String[]{"Konark Road Junction", "Satpada"},
                        new String[]{"Satpada", "Relief Shelter - Community Hall"}
                )
        );
    }

    /** Returns all three case studies in presentation order. */
    public static List<Scenario> allScenarios() {
        return List.of(buildFloodScenario(), buildEarthquakeScenario(), buildCycloneScenario());
    }
}
