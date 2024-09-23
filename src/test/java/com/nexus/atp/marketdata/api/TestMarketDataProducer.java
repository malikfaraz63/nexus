package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;

public interface TestMarketDataProducer {
    StockQuoteIntraDay getNextIntraDayQuote(String ticker);
    StockQuoteDaily getNextDailyQuote(String ticker);
}
