package com.logistics.algorithm;

import java.util.*;

/**
 * Min-Cost Max-Flow (MCMF) Solver using the Successive Shortest Path algorithm
 * with the Shortest Path Faster Algorithm (SPFA) for augmenting paths.
 */
public class McmfSolver {

    public static class McmfResult {
        private final int maxFlow;
        private final double totalCost;
        private final int targetDemand;
        private final int unfulfilledDemand;
        private final List<FlowEdge> activeEdges;
        private final List<String> bottlenecks;

        public McmfResult(int maxFlow, double totalCost, int targetDemand,
                          int unfulfilledDemand, List<FlowEdge> activeEdges,
                          List<String> bottlenecks) {
            this.maxFlow = maxFlow;
            this.totalCost = totalCost;
            this.targetDemand = targetDemand;
            this.unfulfilledDemand = unfulfilledDemand;
            this.activeEdges = activeEdges;
            this.bottlenecks = bottlenecks;
        }

        public int getMaxFlow() {
            return maxFlow;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public int getTargetDemand() {
            return targetDemand;
        }

        public int getUnfulfilledDemand() {
            return unfulfilledDemand;
        }

        public List<FlowEdge> getActiveEdges() {
            return activeEdges;
        }

        public List<String> getBottlenecks() {
            return bottlenecks;
        }

        public boolean isFullyFulfilled() {
            return unfulfilledDemand == 0;
        }
    }

    /**
     * Solves the Min-Cost Max-Flow problem up to targetDemand.
     *
     * @param network Flow network with nodes and directed edges
     * @param source Super-source node ID
     * @param sink Super-sink node ID
     * @param targetDemand Total units needed
     * @return McmfResult containing flow, cost, utilized edges, and bottlenecks
     */
    public static McmfResult solve(FlowNetwork network, int source, int sink, int targetDemand) {
        int n = network.getNumVertices();
        int totalFlow = 0;
        double totalCost = 0.0;

        double[] dist = new double[n];
        FlowEdge[] parentEdge = new FlowEdge[n];
        boolean[] inQueue = new boolean[n];

        while (totalFlow < targetDemand) {
            Arrays.fill(dist, Double.POSITIVE_INFINITY);
            Arrays.fill(parentEdge, null);
            Arrays.fill(inQueue, false);

            Queue<Integer> queue = new ArrayDeque<>();
            dist[source] = 0;
            queue.add(source);
            inQueue[source] = true;

            // SPFA to find the minimum cost augmenting path
            while (!queue.isEmpty()) {
                int u = queue.poll();
                inQueue[u] = false;

                for (FlowEdge edge : network.getAdj(u)) {
                    if (edge.getResidualCapacity() > 0) {
                        double newDist = dist[u] + edge.getCost();
                        // Use small epsilon for double precision stability
                        if (newDist < dist[edge.getTo()] - 1e-9) {
                            dist[edge.getTo()] = newDist;
                            parentEdge[edge.getTo()] = edge;
                            if (!inQueue[edge.getTo()]) {
                                queue.add(edge.getTo());
                                inQueue[edge.getTo()] = true;
                            }
                        }
                    }
                }
            }

            // If no path to sink exists in the residual network, stop
            if (dist[sink] == Double.POSITIVE_INFINITY) {
                break;
            }

            // Find bottleneck capacity along the augmenting path
            int pushFlow = targetDemand - totalFlow;
            int curr = sink;
            while (curr != source) {
                FlowEdge edge = parentEdge[curr];
                pushFlow = Math.min(pushFlow, edge.getResidualCapacity());
                curr = edge.getFrom();
            }

            if (pushFlow <= 0) {
                break;
            }

            // Augment flow along the path
            curr = sink;
            while (curr != source) {
                FlowEdge edge = parentEdge[curr];
                edge.addFlow(pushFlow);
                curr = edge.getFrom();
            }

            totalFlow += pushFlow;
            totalCost += pushFlow * dist[sink];
        }

        // Collect all edges that carried positive flow (excluding super-source/sink edges if not needed)
        List<FlowEdge> activeEdges = new ArrayList<>();
        List<String> bottlenecks = new ArrayList<>();

        for (FlowEdge edge : network.getOriginalEdges()) {
            if (edge.getFlow() > 0) {
                activeEdges.add(edge);
            }
            // Saturated routes that carried flow
            if (edge.getCapacity() > 0 && edge.getResidualCapacity() == 0 && edge.getFlow() > 0) {
                bottlenecks.add(String.format("Route '%s -> %s' reached maximum capacity of %d units.",
                        edge.getFromName(), edge.getToName(), edge.getCapacity()));
            }
        }

        int unfulfilled = targetDemand - totalFlow;
        if (unfulfilled > 0 && bottlenecks.isEmpty()) {
            bottlenecks.add("Insufficient stock available across warehouses or no viable connecting routes.");
        }

        return new McmfResult(totalFlow, Math.round(totalCost * 100.0) / 100.0,
                targetDemand, unfulfilled, activeEdges, bottlenecks);
    }
}
