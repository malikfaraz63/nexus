package com.nexus.atp.marketdata.manager;

import com.nexus.atp.marketdata.quote.StockQuote;

import java.util.Date;
import java.util.List;

public interface MarketDataManager {
    void subscribeToStock(String ticker);
    List<StockQuote> getStockQuotes(String ticker);
    double getMaxPriceForStock(String ticker, Date fromDate, Date toDate);
}
