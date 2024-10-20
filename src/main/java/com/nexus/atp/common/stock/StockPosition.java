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
    private int quantity;
    private int outstandingQuantity;

    public StockPosition(String ticker) {
        this.ticker = ticker;
        this.positionTransactions = new ArrayList<>();
        this.notional = 0;
    }

    /**
     * Adds the latest transaction on the stock position.
     * @param transaction the transaction to be added to this position
     */
    public void addTransaction(TRANSACTION transaction) {
        if (!transaction.ticker().equals(ticker)) {
            throw new IllegalArgumentException("Adding transaction for stock with different ticker");
        }

        positionTransactions.add(transaction);
        notional += transaction.getNotional();
        quantity += transaction.quantity();
        outstandingQuantity += transaction.getOutstandingQuantity();
    }

    public List<TRANSACTION> getTransactions(Date fromDate, Date toDate) {
        return positionTransactions
            .stream()
            .filter(transaction -> transaction.transactionDate().after(fromDate) && transaction.transactionDate().before(toDate))
            .toList();
    }

    public List<TRANSACTION> getOutstandingBuyTransactions() {
        List<TRANSACTION> buyTransactions = new ArrayList<>();
        List<TRANSACTION> sellTransactions = new ArrayList<>();

        for (TRANSACTION transaction : positionTransactions) {
            if (transaction.side() == TradingSide.BUY) {
                buyTransactions.add(transaction);
            } else {
                sellTransactions.add(transaction);
            }
        }

        int buyIndex = 0;
        int buyQuantity = buyTransactions.getFirst().quantity();

        int sellIndex = 0;
        while (sellIndex < sellTransactions.size()) {
            int sellQuantity = sellTransactions.get(sellIndex).quantity();

            while (sellQuantity > 0) {
                int reduction = Math.min(sellQuantity, buyQuantity);
                sellQuantity -= reduction;
                buyQuantity -= reduction;

                if (buyQuantity == 0) {
                    buyIndex++;
                    if (buyIndex == buyTransactions.size()) {
                        return List.of();
                    }

                    buyQuantity = buyTransactions.get(buyIndex).quantity();
                }
            }

            sellIndex++;
        }

        if (buyQuantity < buyTransactions.get(buyIndex).quantity()) {
            TRANSACTION replacementTransaction = buyTransactions.get(buyIndex).withQuantity(buyQuantity);
            buyTransactions.set(buyIndex, replacementTransaction);
        }

        return buyTransactions.subList(buyIndex, buyTransactions.size());
    }

    public double getCoreProfitability(Date fromDate, Date toDate) {
        List<TRANSACTION> transactions = getTransactions(fromDate, toDate);

        int lowerBound = 0;
        while (lowerBound < transactions.size() && transactions.get(lowerBound).side() == TradingSide.SELL) {
            lowerBound++;
        }

        int upperBound = transactions.size();
        while (upperBound > 0 && transactions.get(upperBound - 1).side() == TradingSide.BUY) {
            upperBound--;
        }

        if (lowerBound == upperBound) {
            // TODO: error management and debugging logs
            return 0;
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

    public double getVolume(Date fromDate, Date toDate) {
        return getTransactions(fromDate, toDate)
                .stream()
                .map(TRANSACTION::getVolume)
                .reduce(Double::sum)
                .orElse(0.0);
    }

    public double getBuyVolume(Date fromDate, Date toDate) {
        return getTransactions(fromDate, toDate)
                .stream()
                .filter(transaction -> transaction.side() == TradingSide.BUY)
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
