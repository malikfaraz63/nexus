package com.nexus.atp.algos.common.hold;

public enum StockHoldUnitAllocation {
    LARGE(0.05),
    MEDIUM(0.025),
    SMALL(0.01);

    private final double unitAllocation;

    StockHoldUnitAllocation(double unitAllocation) {
        this.unitAllocation = unitAllocation;
    }

    public double getValue() {
        return unitAllocation;
    }
}
