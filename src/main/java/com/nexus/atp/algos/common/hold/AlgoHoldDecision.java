package com.nexus.atp.algos.common.hold;

import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.StockHold;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgoHoldDecision implements HoldDecision {
    private final Map<StockHoldUnitAllocation, StockHold> allocationToStockHold;
    private final List<StockHold> stockHolds;

    public AlgoHoldDecision(AllocationConfiguration configuration) {
        this.allocationToStockHold = new HashMap<>();
        this.stockHolds = new ArrayList<>();

        configuration.setupStockHolds(allocationToStockHold, stockHolds);
    }

    /**
     *
     * @param tickers
     * @return whether the stock holds have been saturated
     */
    public boolean addStockTickers(List<String> tickers) {
        int tickerIndex = 0;
        for (StockHold stockHold : stockHolds) {
            while (stockHold.addTicker(tickers.get(tickerIndex))) {
                tickerIndex++;

                if (tickerIndex >= tickers.size()) {
                    return areStockHoldAllocationsFull();
                }
            }
        }

        return areStockHoldAllocationsFull();
    }

    private boolean areStockHoldAllocationsFull() {
        return stockHolds.get(stockHolds.size() - 1).isAllocationFull();
    }

    @Override
    public List<StockHold> getStockHolds() {
        return stockHolds;
    }
}
