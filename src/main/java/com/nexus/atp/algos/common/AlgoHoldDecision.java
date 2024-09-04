package com.nexus.atp.algos.common;

import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.StocksHold;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgoHoldDecision implements HoldDecision {
    private final Map<StockHoldUnitAllocation, StocksHold> allocationToStockHold;
    private final List<StocksHold> stocksHolds;

    public AlgoHoldDecision(AllocationConfig configuration) {
        this.allocationToStockHold = new HashMap<>();
        this.stocksHolds = new ArrayList<>();

        configuration.setupStockHolds(allocationToStockHold, stocksHolds);
    }

    /**
     *
     * @param tickers
     * @return whether the stock holds have been saturated
     */
    public boolean addStockTickers(List<String> tickers) {
        int tickerIndex = 0;
        for (StocksHold stocksHold : stocksHolds) {
            while (stocksHold.addTicker(tickers.get(tickerIndex))) {
                tickerIndex++;

                if (tickerIndex == tickers.size()) {
                    return areStockHoldAllocationsFull();
                }
            }
        }

        return areStockHoldAllocationsFull();
    }

    public boolean addStockTicker(String ticker) {
        for (StocksHold stocksHold : stocksHolds) {
            if (stocksHold.addTicker(ticker)) {
                return true;
            }
        }
        return false;
    }

    private boolean areStockHoldAllocationsFull() {
        return stocksHolds.getLast().isAllocationFull();
    }

    @Override
    public List<StocksHold> getStocksHolds() {
        return stocksHolds;
    }
}
