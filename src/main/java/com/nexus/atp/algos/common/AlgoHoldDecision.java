package com.nexus.atp.algos.common;

import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.StocksHold;

import java.util.*;

public class AlgoHoldDecision implements HoldDecision {
    private final Map<StockHoldUnitAllocation, StocksHold> allocationToStockHold;
    private final List<StocksHold> stocksHolds;

    private final Set<String> tickers;

    public AlgoHoldDecision(AllocationConfig configuration) {
        this.allocationToStockHold = new HashMap<>();
        this.stocksHolds = new ArrayList<>();
        this.tickers = new HashSet<>();

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

                tickers.add(tickers.get(tickerIndex));

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

    @Override
    public Set<String> getTickers() {
        return tickers;
    }
}
