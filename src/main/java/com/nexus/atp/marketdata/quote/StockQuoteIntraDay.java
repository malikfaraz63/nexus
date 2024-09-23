package com.nexus.atp.marketdata.quote;

import java.util.Date;

public record StockQuoteIntraDay(String ticker, double price, Date timestamp) {
}
