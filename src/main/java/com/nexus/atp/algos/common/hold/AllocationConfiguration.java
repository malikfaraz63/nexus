package com.nexus.atp.algos.common.hold;

import com.nexus.atp.positions.hold.StockHold;
import java.util.List;
import java.util.Map;

public class AllocationConfiguration {
    private final Map<StockHoldUnitAllocation, Double> allocationLimits;
    private final List<StockHoldUnitAllocation> orderedAllocations;

    public AllocationConfiguration(Map<StockHoldUnitAllocation, Double> allocationLimits, List<StockHoldUnitAllocation> orderedAllocations) {
        if (allocationLimits.isEmpty()) {
            throw new IllegalArgumentException("No allocation limits given");
        }

        if (allocationLimits.values().stream().reduce(Double::sum).get() != 1.0) {
            throw new IllegalArgumentException("Insufficient cumulative allocation limit");
        }

        this.allocationLimits = allocationLimits;
        this.orderedAllocations = orderedAllocations;
    }

    public void setupStockHolds(Map<StockHoldUnitAllocation, StockHold> allocationToStockHold, List<StockHold> stockHolds) {
        for (StockHoldUnitAllocation allocation : orderedAllocations) {
            StockHold stockHold = new StockHold(allocation, allocationLimits.get(allocation));

            stockHolds.add(stockHold);
            allocationToStockHold.put(allocation, stockHold);
        }
    }
}
