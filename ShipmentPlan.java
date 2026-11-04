package com.logistics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment_plans")
public class ShipmentPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int requestedQuantity;
    private int fulfilledQuantity;
    private int unfulfilledQuantity;

    private double totalTransportationCost;
    private double averageUnitCost;

    private String feasibilityStatus; // "OPTIMAL_FULFILLED", "PARTIALLY_FULFILLED", "INFEASIBLE"

    @Column(length = 1000)
    private String bottleneckSummary; // Explains constraints or saturated routes

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shipment_hops", joinColumns = @JoinColumn(name = "plan_id"))
    private List<ShipmentHop> hops = new ArrayList<>();

    private LocalDateTime calculatedAt;

    public ShipmentPlan() {}

    public ShipmentPlan(int requestedQuantity, int fulfilledQuantity, int unfulfilledQuantity,
                        double totalTransportationCost, double averageUnitCost,
                        String feasibilityStatus, String bottleneckSummary,
                        List<ShipmentHop> hops) {
        this.requestedQuantity = requestedQuantity;
        this.fulfilledQuantity = fulfilledQuantity;
        this.unfulfilledQuantity = unfulfilledQuantity;
        this.totalTransportationCost = totalTransportationCost;
        this.averageUnitCost = averageUnitCost;
        this.feasibilityStatus = feasibilityStatus;
        this.bottleneckSummary = bottleneckSummary;
        this.hops = hops != null ? hops : new ArrayList<>();
        this.calculatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public int getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public void setFulfilledQuantity(int fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
    }

    public int getUnfulfilledQuantity() {
        return unfulfilledQuantity;
    }

    public void setUnfulfilledQuantity(int unfulfilledQuantity) {
        this.unfulfilledQuantity = unfulfilledQuantity;
    }

    public double getTotalTransportationCost() {
        return totalTransportationCost;
    }

    public void setTotalTransportationCost(double totalTransportationCost) {
        this.totalTransportationCost = totalTransportationCost;
    }

    public double getAverageUnitCost() {
        return averageUnitCost;
    }

    public void setAverageUnitCost(double averageUnitCost) {
        this.averageUnitCost = averageUnitCost;
    }

    public String getFeasibilityStatus() {
        return feasibilityStatus;
    }

    public void setFeasibilityStatus(String feasibilityStatus) {
        this.feasibilityStatus = feasibilityStatus;
    }

    public String getBottleneckSummary() {
        return bottleneckSummary;
    }

    public void setBottleneckSummary(String bottleneckSummary) {
        this.bottleneckSummary = bottleneckSummary;
    }

    public List<ShipmentHop> getHops() {
        return hops;
    }

    public void setHops(List<ShipmentHop> hops) {
        this.hops = hops;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
