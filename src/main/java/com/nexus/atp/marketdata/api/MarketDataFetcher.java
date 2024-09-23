package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteHandler;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.marketdata.quote.StockQuotesHandler;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface MarketDataFetcher {
    void scheduleStockQuoteDaily(Set<String> tickers,
                                 LocalTime scheduledStart,
                                 StockQuotesHandler<List<StockQuoteDaily>> handler);

    void getHistoricStockQuotes(Set<String> tickers,
                                StockQuotesHandler<List<StockQuoteDaily>> handler);

    void subscribeToStockIntraDay(String ticker,
                                  LocalTime scheduledStart,
                                  long callbackPeriod, TimeUnit periodUnit,
                                  StockQuoteHandler handler);

    void getStockQuotes(Set<String> tickers, StockQuotesHandler<StockQuoteIntraDay> handler);
}
