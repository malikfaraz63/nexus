package com.nexus.atp.positions.hold;

import com.nexus.atp.algos.common.StockHoldUnitAllocation;
import java.util.ArrayList;
import java.util.List;

public class StocksHold {
    private final List<String> tickers;
    private final StockHoldUnitAllocation unitAllocation;
    private final double allocationLimit;

    public StocksHold(StockHoldUnitAllocation unitAllocation, double allocationLimit) {
        this.tickers = new ArrayList<>();
        this.unitAllocation = unitAllocation;
        this.allocationLimit = allocationLimit;
    }

    public boolean addTicker(String ticker) {
        if (getNextAllocation() > allocationLimit) {
            return false;
        }

        tickers.add(ticker);
        return true;
    }

    public List<String> getTickers() {
        return tickers;
    }

    public StockHoldUnitAllocation getUnitAllocation() {
        return unitAllocation;
    }

    public double getTotalAllocation() {
        return unitAllocation.getValue() * tickers.size();
    }

    public boolean isAllocationFull() {
        return getNextAllocation() > allocationLimit;
    }

    private double getNextAllocation() {
        return unitAllocation.getValue() * (tickers.size() + 1);
    }
}
