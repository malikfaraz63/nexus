package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuoteHandler;
import com.nexus.atp.marketdata.quote.StockQuotesHandler;

import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface MarketDataFetcher {
    void subscribeToStock(String ticker,
                          LocalTime scheduledStart,
                          long callbackPeriod, TimeUnit periodUnit,
                          StockQuoteHandler handler);

    void getStockQuotes(Set<String> tickers, StockQuotesHandler handler);
}
