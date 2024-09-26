package com.nexus.atp.marketdata.quote;

import java.util.Date;

public record StockQuoteDaily(String ticker,
                              double open,
                              double high,
                              double low,
                              double close,
                              double volume,
                              Date timestamp) {

}
