package com.nexus.atp.positions.engine;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class StockPositionsEngineConfig {
    private final LocalTime marketDataStartTime;
    private final long marketDataCallbackPeriod;

    public StockPositionsEngineConfig(LocalTime marketDataStartTime, long marketDataCallbackPeriod) {
        this.marketDataStartTime = marketDataStartTime;
        this.marketDataCallbackPeriod = marketDataCallbackPeriod;
    }

    public LocalTime getMarketDataStartTime() {
        return marketDataStartTime;
    }

    public long getMarketDataCallbackPeriod() {
        return marketDataCallbackPeriod;
    }

    public TimeUnit getMarketDataPeriodUnit() {
        return TimeUnit.MINUTES;
    }
}
