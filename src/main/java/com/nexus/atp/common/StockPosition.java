package com.nexus.atp.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages a singular stock position through a series of transactions on that stock
 */
public class StockPosition<TRANSACTION extends BaseTransaction> {
    private final List<TRANSACTION> positionTransactions;

    private final String ticker;
    private double notional;
    private int quantity;

    public StockPosition(String ticker) {
        this.ticker = ticker;
        this.positionTransactions = new ArrayList<>();
        this.notional = 0;
    }

    public void addTransaction(TRANSACTION transaction) {
        if (!transaction.ticker().equals(ticker)) {
            throw new IllegalArgumentException("Adding transaction for stock with different ticker");
        }

        positionTransactions.add(transaction);
        notional += transaction.getNotional();
        quantity += transaction.quantity();
    }

    public List<TRANSACTION> getTransactions(Date fromDate, Date toDate) {
        return positionTransactions
            .stream()
            .filter(transaction -> transaction.transactionDate().after(fromDate) && transaction.transactionDate().before(toDate))
            .toList();
    }

    public double getNotional(Date fromDate, Date toDate) {
        return getTransactions(fromDate, toDate)
            .stream()
            .map(TRANSACTION::getNotional)
            .reduce(Double::sum)
            .orElse(0.0);
    }

    public int getQuantity(Date fromDate, Date toDate) {
        return getTransactions(fromDate, toDate)
            .stream()
            .map(TRANSACTION::quantity)
            .reduce(Integer::sum)
            .orElse(0);
    }

    public int getQuantity() {
        return quantity;
    }

    public double getNotional() {
        return notional;
    }

    public List<TRANSACTION> getTransactions() {
        return positionTransactions;
    }

    @Override
    public int hashCode() {
        return ticker.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StockPosition<?> otherPosition) {
            return ticker.equals(otherPosition.ticker);
        }

        return false;
    }
}
