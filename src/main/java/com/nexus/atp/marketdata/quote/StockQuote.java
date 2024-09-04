package com.nexus.atp.marketdata.quote;

import java.util.Date;

public class StockQuote {
    private final String ticker;
    private final double price;
    private final Date timestamp;

    public StockQuote(String ticker, double price, Date timestamp) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getTicker() {
        return ticker;
    }

    public double getPrice() {
        return price;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
