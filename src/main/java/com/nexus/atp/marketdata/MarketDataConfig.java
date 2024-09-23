package com.nexus.atp.marketdata;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public interface MarketDataConfig {
    LocalTime getEndOfDayStockQuotesTime();
    LocalTime getMarketDataStartTime();
    long getMarketDataCallbackPeriod();
    TimeUnit getMarketDataPeriodUnit();
}
