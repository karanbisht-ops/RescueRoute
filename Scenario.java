package com.disasterops;

import com.disasterops.core.Graph;
import com.disasterops.core.TriageSystem;
import com.disasterops.model.Zone;
import java.util.List;

/**
 * A complete, self-contained disaster case study: a road network, a set of
 * victim reports, a set of relief zones, and the shelters relevant to that
 * scenario. Bundling these together lets the application switch between
 * entirely different disaster case studies (flood, earthquake, cyclone)
 * without any of the core algorithm code changing — only the data changes,
 * which is exactly the separation a well-designed system should have.
 *
 * @author Team DisasterOps
 */
public class Scenario {

    private final String name;
    private final String description;
    private final Graph graph;
    private final TriageSystem triageSystem;
    private final List<Zone> zones;
    private final int supplyCapacity;
    private final String startLocation;
    private final List<String> shelterNames;
    private final String demoBlockedRoadFrom;
    private final String demoBlockedRoadTo;
    private final String isolationDemoLocation;
    private final List<String[]> isolationDemoRoads;

    public Scenario(String name, String description, Graph graph, TriageSystem triageSystem,
                     List<Zone> zones, int supplyCapacity, String startLocation,
                     List<String> shelterNames, String demoBlockedRoadFrom, String demoBlockedRoadTo,
                     String isolationDemoLocation, List<String[]> isolationDemoRoads) {
        this.name = name;
        this.description = description;
        this.graph = graph;
        this.triageSystem = triageSystem;
        this.zones = zones;
        this.supplyCapacity = supplyCapacity;
        this.startLocation = startLocation;
        this.shelterNames = shelterNames;
        this.demoBlockedRoadFrom = demoBlockedRoadFrom;
        this.demoBlockedRoadTo = demoBlockedRoadTo;
        this.isolationDemoLocation = isolationDemoLocation;
        this.isolationDemoRoads = isolationDemoRoads;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Graph getGraph() { return graph; }
    public TriageSystem getTriageSystem() { return triageSystem; }
    public List<Zone> getZones() { return zones; }
    public int getSupplyCapacity() { return supplyCapacity; }
    public String getStartLocation() { return startLocation; }
    public List<String> getShelterNames() { return shelterNames; }
    public String getDemoBlockedRoadFrom() { return demoBlockedRoadFrom; }
    public String getDemoBlockedRoadTo() { return demoBlockedRoadTo; }
    public String getIsolationDemoLocation() { return isolationDemoLocation; }
    public List<String[]> getIsolationDemoRoads() { return isolationDemoRoads; }
}
