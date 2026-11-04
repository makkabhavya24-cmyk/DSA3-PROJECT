package com.logistics.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class ShipmentHop {
    private String fromNode;
    private String toNode;
    private int quantity;
    private double unitCost;
    private double subtotalCost;
    private double distanceKm;
    private double travelTimeHours;
    private Long routeId;

    public ShipmentHop() {}

    public ShipmentHop(String fromNode, String toNode, int quantity, double unitCost, double subtotalCost, double distanceKm, double travelTimeHours, Long routeId) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.subtotalCost = subtotalCost;
        this.distanceKm = distanceKm;
        this.travelTimeHours = travelTimeHours;
        this.routeId = routeId;
    }

    public String getFromNode() {
        return fromNode;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getToNode() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public double getSubtotalCost() {
        return subtotalCost;
    }

    public void setSubtotalCost(double subtotalCost) {
        this.subtotalCost = subtotalCost;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getTravelTimeHours() {
        return travelTimeHours;
    }

    public void setTravelTimeHours(double travelTimeHours) {
        this.travelTimeHours = travelTimeHours;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
