package com.nexus.atp.common;

import java.util.Date;
import java.util.Objects;

public abstract class BaseTransaction implements Comparable<BaseTransaction> {
    private final String ticker;
    private final int quantity;
    private final double price;
    private final TradingSide side;
    private final Date transactionDate;

    public BaseTransaction(
        String ticker,
        int quantity,
        double price,
        TradingSide side,
        Date transactionDate
    ) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.transactionDate = transactionDate;
    }

    public String ticker() {
        return ticker;
    }

    public int quantity() {
        return quantity;
    }

    public double price() {
        return price;
    }

    public double getNotional() {
        return switch (side) {
            case BUY ->
                quantity * price;
            case SELL ->
                quantity * -price;
        };
    }

    public TradingSide side() {
        return side;
    }

    public Date transactionDate() {
        return transactionDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, transactionDate);
    }

    @Override
    public int compareTo(BaseTransaction otherTransaction) {
        return transactionDate.compareTo(otherTransaction.transactionDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseTransaction otherTransaction) {
            return Objects.equals(ticker, otherTransaction.ticker);
        }
        return false;
    }
}
