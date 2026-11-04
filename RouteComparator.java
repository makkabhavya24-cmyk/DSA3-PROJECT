package com.logistics.algorithm;

import com.logistics.model.Route;

import java.util.*;

/**
 * Route Comparator using Graph Algorithms (Dijkstra and Yen's k-shortest paths variation)
 * to compare the Cheapest Best Route vs. the Second-Best Alternative Route.
 */
public class RouteComparator {

    private static class GraphEdge {
        String from;
        String to;
        double cost;
        int capacity;
        double distanceKm;
        double travelTimeHours;
        Long routeId;

        GraphEdge(Route r) {
            this.from = r.getSourceName();
            this.to = r.getDestinationName();
            this.cost = r.getUnitCost();
            this.capacity = r.getCapacity();
            this.distanceKm = r.getDistanceKm();
            this.travelTimeHours = r.getTravelTimeHours();
            this.routeId = r.getId() != null ? r.getId() : (long) Objects.hash(r.getSourceName(), r.getDestinationName(), r.getRouteCode());
        }
    }

    private static class PathCandidate {
        List<GraphEdge> edges = new ArrayList<>();
        double totalCost = 0.0;

        PathCandidate() {}

        PathCandidate(PathCandidate other) {
            this.edges.addAll(other.edges);
            this.totalCost = other.totalCost;
        }

        void addEdge(GraphEdge e) {
            edges.add(e);
            totalCost += e.cost;
        }

        String getLastNode(String startNode) {
            if (edges.isEmpty()) return startNode;
            return edges.get(edges.size() - 1).to;
        }
    }

    public static RouteAlternativeResult compareRoutes(List<Route> allRoutes, String startNode, String endNode) {
        // Filter active routes only
        List<GraphEdge> edges = new ArrayList<>();
        for (Route r : allRoutes) {
            if (r.isActive()) {
                edges.add(new GraphEdge(r));
            }
        }

        // 1. Find shortest (cheapest) path using Dijkstra
        PathCandidate bestCandidate = findCheapestPath(edges, startNode, endNode, Collections.emptySet());
        if (bestCandidate == null) {
            return new RouteAlternativeResult(startNode, endNode, null, null,
                    "No active route path exists between '" + startNode + "' and '" + endNode + "'.");
        }

        RouteAlternativeResult.SingleRoutePath bestPath = buildSingleRoutePath("Best Route (Cheapest)", bestCandidate, startNode);

        // 2. Find 2nd best alternative path by temporarily excluding each edge from the best path
        PathCandidate secondBestCandidate = null;
        for (GraphEdge excludedEdge : bestCandidate.edges) {
            Set<Long> excludedRouteIds = Collections.singleton(excludedEdge.routeId);
            PathCandidate alternative = findCheapestPath(edges, startNode, endNode, excludedRouteIds);
            if (alternative != null) {
                // Check if alternative is different from best path
                boolean isDifferent = alternative.edges.size() != bestCandidate.edges.size();
                if (!isDifferent) {
                    for (int i = 0; i < alternative.edges.size(); i++) {
                        if (!alternative.edges.get(i).routeId.equals(bestCandidate.edges.get(i).routeId)) {
                            isDifferent = true;
                            break;
                        }
                    }
                }
                if (isDifferent) {
                    if (secondBestCandidate == null || alternative.totalCost < secondBestCandidate.totalCost) {
                        secondBestCandidate = alternative;
                    }
                }
            }
        }

        RouteAlternativeResult.SingleRoutePath secondBestPath = null;
        String analysis;

        if (secondBestCandidate != null) {
            secondBestPath = buildSingleRoutePath("Second-Best Alternative", secondBestCandidate, startNode);
            double costDiff = secondBestPath.getTotalUnitCost() - bestPath.getTotalUnitCost();
            double timeDiff = secondBestPath.getTotalTravelTimeHours() - bestPath.getTotalTravelTimeHours();
            analysis = String.format("The primary route saves ₹%.2f/unit compared to the alternative. " +
                            "If primary capacity (%d units) is exceeded or route is blocked, alternative route handles up to %d units with a travel time of %.1f hrs (diff: %+.1f hrs).",
                    costDiff, bestPath.getBottleneckCapacity(), secondBestPath.getBottleneckCapacity(),
                    secondBestPath.getTotalTravelTimeHours(), timeDiff);
        } else {
            analysis = "No second alternative path currently available under active route constraints. All traffic must flow through the primary route.";
        }

        return new RouteAlternativeResult(startNode, endNode, bestPath, secondBestPath, analysis);
    }

    private static PathCandidate findCheapestPath(List<GraphEdge> edges, String start, String end, Set<Long> excludedRouteIds) {
        // Build adjacency map
        Map<String, List<GraphEdge>> adj = new HashMap<>();
        for (GraphEdge e : edges) {
            if (excludedRouteIds.contains(e.routeId)) continue;
            adj.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e);
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, GraphEdge> prevEdge = new HashMap<>();
        Map<String, String> prevNode = new HashMap<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.dist));

        dist.put(start, 0.0);
        pq.add(new NodeDist(start, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            if (current.dist > dist.getOrDefault(current.node, Double.POSITIVE_INFINITY)) continue;
            if (current.node.equals(end)) break;

            List<GraphEdge> neighbors = adj.getOrDefault(current.node, Collections.emptyList());
            for (GraphEdge edge : neighbors) {
                double newDist = current.dist + edge.cost;
                if (newDist < dist.getOrDefault(edge.to, Double.POSITIVE_INFINITY)) {
                    dist.put(edge.to, newDist);
                    prevEdge.put(edge.to, edge);
                    prevNode.put(edge.to, current.node);
                    pq.add(new NodeDist(edge.to, newDist));
                }
            }
        }

        if (!dist.containsKey(end)) return null;

        // Reconstruct path
        List<GraphEdge> pathEdges = new ArrayList<>();
        String curr = end;
        while (!curr.equals(start)) {
            GraphEdge edge = prevEdge.get(curr);
            if (edge == null) return null;
            pathEdges.add(0, edge);
            curr = prevNode.get(curr);
        }

        PathCandidate candidate = new PathCandidate();
        for (GraphEdge e : pathEdges) {
            candidate.addEdge(e);
        }
        return candidate;
    }

    private static RouteAlternativeResult.SingleRoutePath buildSingleRoutePath(String rank, PathCandidate candidate, String startNode) {
        List<String> nodeSeq = new ArrayList<>();
        nodeSeq.add(startNode);
        List<Long> routeIds = new ArrayList<>();

        double totalDist = 0.0;
        double totalTime = 0.0;
        int bottleneckCap = Integer.MAX_VALUE;

        for (GraphEdge e : candidate.edges) {
            nodeSeq.add(e.to);
            routeIds.add(e.routeId);
            totalDist += e.distanceKm;
            totalTime += e.travelTimeHours;
            bottleneckCap = Math.min(bottleneckCap, e.capacity);
        }

        if (bottleneckCap == Integer.MAX_VALUE) bottleneckCap = 0;
        String desc = String.join(" -> ", nodeSeq);

        return new RouteAlternativeResult.SingleRoutePath(rank, nodeSeq, desc, candidate.totalCost,
                totalDist, bottleneckCap, totalTime, routeIds);
    }

    private static class NodeDist {
        String node;
        double dist;

        NodeDist(String node, double dist) {
            this.node = node;
            this.dist = dist;
        }
    }
}
