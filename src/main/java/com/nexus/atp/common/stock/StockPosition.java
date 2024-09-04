package com.nexus.atp.common.stock;

import com.nexus.atp.common.transaction.BaseTransaction;
import com.nexus.atp.common.transaction.TradingSide;

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
    private double volume;
    private int quantity;
    private int outstandingQuantity;

    public StockPosition(String ticker) {
        this.ticker = ticker;
        this.positionTransactions = new ArrayList<>();
        this.notional = 0;
    }

    /**
     * Adds the latest transaction.
     * @param transaction
     */
    public void addTransaction(TRANSACTION transaction) {
        if (!transaction.ticker().equals(ticker)) {
            throw new IllegalArgumentException("Adding transaction for stock with different ticker");
        }

        positionTransactions.add(transaction);
        notional += transaction.getNotional();
        volume += transaction.getVolume();
        quantity += transaction.quantity();
        outstandingQuantity += transaction.getOutstandingQuantity();
    }

    public List<TRANSACTION> getTransactions(Date fromDate, Date toDate) {
        return positionTransactions
            .stream()
            .filter(transaction -> transaction.transactionDate().after(fromDate) && transaction.transactionDate().before(toDate))
            .toList();
    }

    public double getCoreProfitability(Date fromDate, Date toDate) {
        List<TRANSACTION> transactions = getTransactions(fromDate, toDate);

        int lowerBound = 0;
        while (transactions.get(lowerBound).side() == TradingSide.SELL) {
            lowerBound++;
        }

        int upperBound = transactions.size();
        while (transactions.get(upperBound - 1).side() == TradingSide.BUY) {
            upperBound--;
        }

        double exposure = transactions.subList(lowerBound, upperBound)
                .stream()
                .map(TRANSACTION::getNotional)
                .reduce(Double::sum)
                .orElse(0.0);

        double price = transactions.subList(lowerBound, upperBound)
                .stream()
                .filter(transaction -> transaction.side() == TradingSide.BUY)
                .map(TRANSACTION::getNotional)
                .reduce(Double::sum)
                .orElse(0.0);

        return (-exposure) / price;
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

    public String getTicker() {
        return ticker;
    }

    public double getVolume() {
        return volume;
    }

    public double getVolume(Date fromDate, Date toDate) {
        return getTransactions(fromDate, toDate)
                .stream()
                .map(TRANSACTION::getVolume)
                .reduce(Double::sum)
                .orElse(0.0);
    }

    public int getQuantity() {
        return quantity;
    }

    public int getOutstandingQuantity() {
        return outstandingQuantity;
    }

    public boolean hasOutstandingQuantity() {
        return outstandingQuantity > 0;
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
