package com.logistics.algorithm;

import java.util.*;

/**
 * Residual Flow Network graph representation for Min-Cost Max-Flow.
 */
public class FlowNetwork {
    private final int numVertices;
    private final List<List<FlowEdge>> adj;
    private final List<FlowEdge> originalEdges;

    private final Map<String, Integer> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();

    public FlowNetwork(int numVertices) {
        this.numVertices = numVertices;
        this.adj = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            this.adj.add(new ArrayList<>());
        }
        this.originalEdges = new ArrayList<>();
    }

    public void registerNode(int id, String name) {
        nameToId.put(name, id);
        idToName.put(id, name);
    }

    public Integer getNodeId(String name) {
        return nameToId.get(name);
    }

    public String getNodeName(int id) {
        return idToName.getOrDefault(id, "Node-" + id);
    }

    /**
     * Adds a directed flow edge with given capacity and cost,
     * along with its reverse residual edge with 0 capacity and -cost.
     */
    public FlowEdge addEdge(int from, int to, int capacity, double cost,
                            Long routeId, String fromName, String toName,
                            double distanceKm, double travelTimeHours) {
        FlowEdge forward = new FlowEdge(from, to, capacity, cost, routeId, fromName, toName, distanceKm, travelTimeHours);
        FlowEdge backward = new FlowEdge(to, from, 0, -cost, routeId, toName, fromName, distanceKm, travelTimeHours);

        forward.setResidual(backward);
        backward.setResidual(forward);

        adj.get(from).add(forward);
        adj.get(to).add(backward);

        originalEdges.add(forward);
        return forward;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public List<FlowEdge> getAdj(int v) {
        return adj.get(v);
    }

    public List<FlowEdge> getOriginalEdges() {
        return Collections.unmodifiableList(originalEdges);
    }
}
