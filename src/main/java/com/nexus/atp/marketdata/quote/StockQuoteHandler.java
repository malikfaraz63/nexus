package com.nexus.atp.marketdata.quote;

public interface StockQuoteHandler {
    void onStockQuote(StockQuoteIntraDay stockQuote);
}
