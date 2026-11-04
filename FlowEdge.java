package com.logistics.algorithm;

/**
 * Directed residual edge for the Min-Cost Max-Flow (MCMF) algorithm.
 */
public class FlowEdge {
    private final int from;
    private final int to;
    private final int capacity;
    private int flow;
    private final double cost; // Unit transportation cost
    private FlowEdge residual;

    private Long routeId;
    private String fromName;
    private String toName;
    private double distanceKm;
    private double travelTimeHours;

    public FlowEdge(int from, int to, int capacity, double cost,
                    Long routeId, String fromName, String toName,
                    double distanceKm, double travelTimeHours) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
        this.cost = cost;
        this.routeId = routeId;
        this.fromName = fromName;
        this.toName = toName;
        this.distanceKm = distanceKm;
        this.travelTimeHours = travelTimeHours;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public double getCost() {
        return cost;
    }

    public FlowEdge getResidual() {
        return residual;
    }

    public void setResidual(FlowEdge residual) {
        this.residual = residual;
    }

    public int getResidualCapacity() {
        return capacity - flow;
    }

    public void addFlow(int delta) {
        this.flow += delta;
        if (this.residual != null) {
            this.residual.flow -= delta;
        }
    }

    public Long getRouteId() {
        return routeId;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToName() {
        return toName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTravelTimeHours() {
        return travelTimeHours;
    }
}
