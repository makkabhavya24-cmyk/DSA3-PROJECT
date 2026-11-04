package com.logistics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeCode; // e.g. "RT-CHI-EAST", "RT-CHI-BOS"

    @Column(nullable = false)
    private String sourceName; // e.g. "Chicago Central Hub"

    private Long sourceWarehouseId; // null if destination, or warehouse ID

    @Column(nullable = false)
    private String destinationName; // e.g. "New York East Hub" or "Boston Retail Center"

    private Long destinationWarehouseId; // null if delivery customer/destination

    @Column(nullable = false)
    private String routeType; // "WAREHOUSE_TO_WAREHOUSE" or "WAREHOUSE_TO_DESTINATION"

    @Column(nullable = false)
    private double unitCost; // Transportation cost per unit ($)

    @Column(nullable = false)
    private int capacity; // Maximum shipping capacity (units)

    private double distanceKm; // Distance in kilometers

    private double travelTimeHours; // Estimated transit time in hours

    private boolean active = true; // Enabled/disabled switch

    public Route() {}

    public Route(String routeCode, String sourceName, Long sourceWarehouseId,
                 String destinationName, Long destinationWarehouseId,
                 String routeType, double unitCost, int capacity,
                 double distanceKm, double travelTimeHours, boolean active) {
        this.routeCode = routeCode;
        this.sourceName = sourceName;
        this.sourceWarehouseId = sourceWarehouseId;
        this.destinationName = destinationName;
        this.destinationWarehouseId = destinationWarehouseId;
        this.routeType = routeType;
        this.unitCost = unitCost;
        this.capacity = capacity;
        this.distanceKm = distanceKm;
        this.travelTimeHours = travelTimeHours;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Long getSourceWarehouseId() {
        return sourceWarehouseId;
    }

    public void setSourceWarehouseId(Long sourceWarehouseId) {
        this.sourceWarehouseId = sourceWarehouseId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Long getDestinationWarehouseId() {
        return destinationWarehouseId;
    }

    public void setDestinationWarehouseId(Long destinationWarehouseId) {
        this.destinationWarehouseId = destinationWarehouseId;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
