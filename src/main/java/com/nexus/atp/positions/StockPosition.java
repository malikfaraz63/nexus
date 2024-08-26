package com.nexus.atp.positions;

import java.util.ArrayList;
import java.util.List;

public class StockPosition {
    private final List<PositionTransaction> positionTransactions;

    private final String ticker;
    private double notional;
    private int quantity;

    public StockPosition(String ticker) {
        this.ticker = ticker;
        this.positionTransactions = new ArrayList<>();
        this.notional = 0;
    }

    public void addTransaction(PositionTransaction transaction) {
        if (!transaction.ticker().equals(ticker)) {
            throw new IllegalArgumentException("Adding transaction for stock with different ticker");
        }

        positionTransactions.add(transaction);

        double transactionNotional = transaction.quantity() * transaction.price();
        switch (transaction.side()) {
            case BUY:
                notional += transactionNotional;
                break;
            case SELL:
                notional -= transactionNotional;
                break;
        }
        quantity += transaction.quantity();
    }

    public int getQuantity() {
        return quantity;
    }

    public double getNotional() {
        return notional;
    }

    public List<PositionTransaction> getTransactions() {
        return positionTransactions;
    }
}
