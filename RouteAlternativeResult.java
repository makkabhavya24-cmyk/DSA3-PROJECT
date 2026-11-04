package com.logistics.algorithm;

import java.util.List;

public class RouteAlternativeResult {

    public static class SingleRoutePath {
        private String rank; // "Best Route" or "Second-Best Alternative"
        private List<String> nodeSequence;
        private String routeDescription;
        private double totalUnitCost;
        private double totalDistanceKm;
        private int bottleneckCapacity;
        private double totalTravelTimeHours;
        private List<Long> routeIds;

        public SingleRoutePath() {}

        public SingleRoutePath(String rank, List<String> nodeSequence, String routeDescription,
                               double totalUnitCost, double totalDistanceKm, int bottleneckCapacity,
                               double totalTravelTimeHours, List<Long> routeIds) {
            this.rank = rank;
            this.nodeSequence = nodeSequence;
            this.routeDescription = routeDescription;
            this.totalUnitCost = Math.round(totalUnitCost * 100.0) / 100.0;
            this.totalDistanceKm = Math.round(totalDistanceKm * 10.0) / 10.0;
            this.bottleneckCapacity = bottleneckCapacity;
            this.totalTravelTimeHours = Math.round(totalTravelTimeHours * 10.0) / 10.0;
            this.routeIds = routeIds;
        }

        public String getRank() {
            return rank;
        }

        public List<String> getNodeSequence() {
            return nodeSequence;
        }

        public String getRouteDescription() {
            return routeDescription;
        }

        public double getTotalUnitCost() {
            return totalUnitCost;
        }

        public double getTotalDistanceKm() {
            return totalDistanceKm;
        }

        public int getBottleneckCapacity() {
            return bottleneckCapacity;
        }

        public double getTotalTravelTimeHours() {
            return totalTravelTimeHours;
        }

        public List<Long> getRouteIds() {
            return routeIds;
        }
    }

    private String sourceName;
    private String destinationName;
    private SingleRoutePath bestRoute;
    private SingleRoutePath secondBestRoute;
    private String analysis;

    public RouteAlternativeResult() {}

    public RouteAlternativeResult(String sourceName, String destinationName,
                                  SingleRoutePath bestRoute, SingleRoutePath secondBestRoute,
                                  String analysis) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.bestRoute = bestRoute;
        this.secondBestRoute = secondBestRoute;
        this.analysis = analysis;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public SingleRoutePath getBestRoute() {
        return bestRoute;
    }

    public SingleRoutePath getSecondBestRoute() {
        return secondBestRoute;
    }

    public String getAnalysis() {
        return analysis;
    }
}
