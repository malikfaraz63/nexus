package com.nexus.atp.algos.common;

import com.nexus.atp.positions.hold.StocksHold;
import java.util.List;
import java.util.Map;

public class AllocationConfig {
    private final Map<StockHoldUnitAllocation, Double> allocationLimits;
    private final List<StockHoldUnitAllocation> orderedAllocations;

    public AllocationConfig(Map<StockHoldUnitAllocation, Double> allocationLimits, List<StockHoldUnitAllocation> orderedAllocations) {
        if (allocationLimits.isEmpty()) {
            throw new IllegalArgumentException("No allocation limits given");
        }

        if (allocationLimits.values().stream().reduce(Double::sum).get() != 1.0) {
            throw new IllegalArgumentException("Insufficient cumulative allocation limit");
        }

        this.allocationLimits = allocationLimits;
        this.orderedAllocations = orderedAllocations;
    }

    public void setupStockHolds(Map<StockHoldUnitAllocation, StocksHold> allocationToStockHold, List<StocksHold> stocksHolds) {
        for (StockHoldUnitAllocation allocation : orderedAllocations) {
            StocksHold stocksHold = new StocksHold(allocation, allocationLimits.get(allocation));

            stocksHolds.add(stocksHold);
            allocationToStockHold.put(allocation, stocksHold);
        }
    }
}
