package com.nexus.atp.marketdata.quote;

import java.util.Map;

public interface StockQuotesHandler<QUOTE> {
    void onStockQuotes(Map<String, QUOTE> stockQuote);
}
