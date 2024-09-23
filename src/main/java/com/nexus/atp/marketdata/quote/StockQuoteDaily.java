package com.nexus.atp.marketdata.quote;

import java.util.Date;

public class StockQuoteDaily {
    private final String ticker;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
    private final Date timestamp;

    public StockQuoteDaily(String ticker, double open, double high, double low, double close, double volume, Date timestamp) {
        this.ticker = ticker;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public String getTicker() {
        return ticker;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getVolume() {
        return volume;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
