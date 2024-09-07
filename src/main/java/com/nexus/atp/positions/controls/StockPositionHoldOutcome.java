package com.nexus.atp.positions.controls;

import java.util.Objects;

public final class StockPositionHoldOutcome {
    private final StockPositionHoldType type;
    private final int sellQuantity;

    private StockPositionSellReason sellReason;

    public StockPositionHoldOutcome(StockPositionHoldType type, int sellQuantity) {
        this.type = type;
        this.sellQuantity = sellQuantity;
    }

    public StockPositionHoldOutcome(StockPositionHoldType type, int sellQuantity, StockPositionSellReason sellReason) {
        this.type = type;
        this.sellQuantity = sellQuantity;
        this.sellReason = sellReason;
    }

    public int sellQuantity() {
        if (type == StockPositionHoldType.HOLD) {
            throw new IllegalStateException("Cannot get sell quantity when outcome is to hold");
        }

        return sellQuantity;
    }

    public StockPositionHoldType type() {
        return type;
    }

    public StockPositionSellReason sellReason() {
        return sellReason;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StockPositionHoldOutcome) obj;
        return Objects.equals(this.type, that.type) &&
                this.sellQuantity == that.sellQuantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sellQuantity);
    }

    @Override
    public String toString() {
        return "StockPositionHoldOutcome[" +
                "type=" + type + ", " +
                "sellQuantity=" + sellQuantity + ']';
    }

}
