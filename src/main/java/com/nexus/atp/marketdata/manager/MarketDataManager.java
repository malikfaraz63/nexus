package com.nexus.atp.marketdata.manager;

import com.nexus.atp.marketdata.quote.StockQuoteDaily;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface MarketDataManager {
    void subscribeToStocks(Set<String> tickers);
    List<StockQuoteDaily> getStockQuotes(String ticker);
    double getMaxPriceForStock(String ticker, Date fromDate, Date toDate);
    StockQuoteDaily getStockQuote(String ticker, Date transactionDate);
}
