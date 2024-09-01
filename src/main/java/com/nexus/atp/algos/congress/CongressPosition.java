package com.nexus.atp.algos.congress;

import com.nexus.atp.common.StockPosition;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CongressPosition {
    private final String congressId;

    private final Map<String, StockPosition<CongressTransaction>> stockTickerToPosition;
    private final Set<StockPosition<CongressTransaction>> stockPositions;

    public CongressPosition(String congressId) {
        this.congressId = congressId;

        this.stockTickerToPosition = new HashMap<>();
        this.stockPositions = new HashSet<>();
    }

    public void addTransaction(CongressTransaction transaction) {
        if (!transaction.congressId().equals(congressId)) {
            throw new IllegalArgumentException("Adding transaction for congressman with different ID");
        }

        StockPosition<CongressTransaction> position = stockTickerToPosition.computeIfAbsent(transaction.ticker(), ticker -> {
            StockPosition<CongressTransaction> stockPosition = new StockPosition<>(ticker);
            stockPositions.add(stockPosition);

            return stockPosition;
        });

        position.addTransaction(transaction);
    }

    public Set<StockPosition<CongressTransaction>> getStockPositions() {
        return stockPositions;
    }

    public double getNotional(Date fromDate, Date toDate) {
        return stockPositions
            .stream()
            .map(stockPosition -> stockPosition.getNotional(fromDate, toDate))
            .reduce(0.0, Double::sum);
    }

    public int getQuantity(Date fromDate, Date toDate) {
        return stockPositions
            .stream()
            .map(stockPosition -> stockPosition.getQuantity(fromDate, toDate))
            .reduce(0, Integer::sum);
    }
}
