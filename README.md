# RescueRoute

**Offline Disaster Response System** — Smart India Hackathon 2026

## Problem Statement

During disasters (earthquakes, floods, etc.), internet and cellular networks often go down exactly when coordination matters most. Relief teams on the ground need a way to triage victims, route them to shelters, and allocate limited resources — **without depending on live connectivity.**

## What RescueRoute Does

RescueRoute is a lightweight, offline-first Java system that helps relief coordinators make fast, informed decisions during a disaster response:

- **Victim Triage** — Ranks incoming victim reports by severity so the most critical cases are handled first, regardless of the order they're reported in.
- **Shelter Routing** — Automatically finds the nearest available shelter for each victim, and recalculates routes if a path becomes blocked (e.g. damaged roads).
- **Resource Allocation** — Distributes limited relief supplies (food, medical kits, etc.) across affected areas to help the maximum number of people, verified against a brute-force cross-check.
- **Network Resilience Check** — Identifies areas that become completely cut off when multiple roads/routes are damaged.

All of this runs **fully offline** — no internet or server dependency — making it usable directly by ground teams during real disruptions.

## Project Structure

```
src/
├── Main.java        # Entry point — run this to start the simulation
├── DemoData.java     # Sample/test data for scenarios
├── Scenario.java     # Core simulation logic (victims, shelters, routing, allocation)
└── SelfTest.java      # Automated tests validating triage, routing, and allocation logic
```

## How to Run

1. Clone this repository:
   ```
   git clone https://github.com/karanbisht-ops/RescueRoute.git
   ```
2. Compile the source files:
   ```
   javac *.java
   ```
3. Run the program:
   ```
   java Main
   ```
4. Follow the on-screen prompts to select a scenario and view triage, routing, and resource allocation results.

## Case Study

See the presentation for a detailed validation case study simulating an earthquake response scenario in Dehradun, Uttarakhand (Seismic Zone IV/V) — including triage timing, shelter rerouting, and optimal resource distribution across affected clusters. All figures are reproducible directly from this code.

## Team

Built for Smart India Hackathon 2026.

## Disclaimer

This is a prototype built for a hackathon. All case study results are from simulated/illustrative data, not a live field deployment.
