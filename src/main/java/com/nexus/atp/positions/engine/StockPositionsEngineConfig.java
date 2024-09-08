package com.nexus.atp.positions.engine;

import com.nexus.atp.marketdata.MarketDataConfig;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class StockPositionsEngineConfig implements MarketDataConfig {
    private final LocalTime marketDataStartTime;
    private final long marketDataCallbackPeriod;

    public StockPositionsEngineConfig(LocalTime marketDataStartTime, long marketDataCallbackPeriod) {
        this.marketDataStartTime = marketDataStartTime;
        this.marketDataCallbackPeriod = marketDataCallbackPeriod;
    }

    @Override
    public LocalTime getMarketDataStartTime() {
        return marketDataStartTime;
    }

    @Override
    public long getMarketDataCallbackPeriod() {
        return marketDataCallbackPeriod;
    }

    @Override
    public TimeUnit getMarketDataPeriodUnit() {
        return TimeUnit.MINUTES;
    }
}
