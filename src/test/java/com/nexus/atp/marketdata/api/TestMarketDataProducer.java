package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuote;

public interface TestMarketDataProducer {
    StockQuote getNextQuote(String ticker);
}
