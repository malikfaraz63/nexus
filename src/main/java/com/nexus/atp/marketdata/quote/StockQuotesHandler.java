package com.nexus.atp.marketdata.quote;

import java.util.Map;

public interface StockQuotesHandler {
    void onStockQuotes(Map<String, StockQuote> stockQuote);
}
