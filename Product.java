package com.logistics.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku; // e.g. "PRD-ELEC-01"

    @Column(nullable = false)
    private String name; // e.g. "Industrial Microcontrollers"

    private String category; // Electronics, Pharmaceuticals, Automotive, etc.

    private String description;

    private double unitPrice;

    private double unitWeightKg;

    private int minStockThreshold; // Triggers low-stock alert if warehouse inventory < threshold

    public Product() {}

    public Product(String sku, String name, String category, String description, double unitPrice, double unitWeightKg, int minStockThreshold) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.description = description;
        this.unitPrice = unitPrice;
        this.unitWeightKg = unitWeightKg;
        this.minStockThreshold = minStockThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getUnitWeightKg() {
        return unitWeightKg;
    }

    public void setUnitWeightKg(double unitWeightKg) {
        this.unitWeightKg = unitWeightKg;
    }

    public int getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(int minStockThreshold) {
        this.minStockThreshold = minStockThreshold;
    }
}
