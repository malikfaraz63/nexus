package com.nexus.atp.common.transaction;

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

    public double getVolume() {
        return quantity * price;
    }

    public int getOutstandingQuantity() {
        return switch (side) {
            case BUY -> quantity;
            case SELL -> -quantity;
        };
    }

    public TradingSide side() {
        return side;
    }

    public Date transactionDate() {
        return transactionDate;
    }

    public abstract <TRANSACTION extends BaseTransaction> TRANSACTION withQuantity(int quantity);

    @Override
    public int hashCode() {
        return Objects.hash(ticker, quantity, price, transactionDate);
    }

    @Override
    public int compareTo(BaseTransaction otherTransaction) {
        return transactionDate.compareTo(otherTransaction.transactionDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseTransaction otherTransaction) {
            return this.hashCode() == otherTransaction.hashCode();
        }
        return false;
    }
}
