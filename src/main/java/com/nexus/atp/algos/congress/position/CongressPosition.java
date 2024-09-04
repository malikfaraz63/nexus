package com.nexus.atp.algos.congress.position;

import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.common.stock.StockPosition;

import java.util.*;

public class CongressPosition {
    private final String congressId;

    private final Map<String, StockPosition<CongressTransaction>> stockTickerToPosition;
    private final Set<StockPosition<CongressTransaction>> stockPositions;

    private boolean wasUpdated = false;

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

        wasUpdated = true;
    }

    public boolean wasUpdated() {
        return wasUpdated;
    }

    public void didViewUpdate() {
        wasUpdated = false;
    }

    public String getCongressId() {
        return congressId;
    }

    public StockPosition<CongressTransaction> getStockPosition(String ticker) {
        return stockTickerToPosition.get(ticker);
    }

    public Set<StockPosition<CongressTransaction>> getStockPositions() {
        return stockPositions;
    }

    public double getVolumeAdjustedProfitability(Date fromDate, Date toDate) {
        return stockPositions
                .stream()
                .map(stock -> stock.getCoreProfitability(fromDate, toDate) * stock.getVolume(fromDate, toDate))
                .reduce(Double::sum)
                .orElse(0.0)
                / stockPositions.size();
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

    @Override
    public int hashCode() {
        return congressId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CongressPosition other) {
            return congressId.equals(other.congressId);
        }
        if (obj instanceof String other) {
            return congressId.equals(other);
        }

        return false;
    }
}
