package com.logistics.dto;

public class StockAdjustDto {
    private int deltaQuantity; // e.g. +50 or -20

    public StockAdjustDto() {}

    public StockAdjustDto(int deltaQuantity) {
        this.deltaQuantity = deltaQuantity;
    }

    public int getDeltaQuantity() {
        return deltaQuantity;
    }

    public void setDeltaQuantity(int deltaQuantity) {
        this.deltaQuantity = deltaQuantity;
    }
}
