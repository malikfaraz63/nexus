package com.nexus.atp.positions.manager;

import com.nexus.atp.positions.PositionTransaction;
import com.nexus.atp.common.StockPosition;
import com.nexus.atp.common.BaseTransactionStorageManager;
import com.nexus.atp.common.TradingSide;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class StockPositionsStorageManager
    extends BaseTransactionStorageManager<PositionTransaction> implements StockPositionsManager {

    private List<PositionTransaction> transactions;

    private final Map<String, StockPosition<PositionTransaction>> stockTickerToPosition;
    private final Set<StockPosition<PositionTransaction>> stockPositions;

    public StockPositionsStorageManager(String filePath) {
        super(filePath, "positionTransactions");
        this.stockTickerToPosition = new HashMap<>();
        this.stockPositions = new HashSet<>();
    }

    @Override
    public Set<StockPosition<PositionTransaction>> getStockPositions() {
        if (transactions == null) {
            this.transactions = super.getTransactions();

            for (PositionTransaction transaction : transactions) {
                putTransaction(transaction);
            }
        }

        return stockPositions;
    }

    private void putTransaction(PositionTransaction transaction) {
        StockPosition<PositionTransaction> position = stockTickerToPosition.computeIfAbsent(transaction.ticker(), ticker -> {
            StockPosition<PositionTransaction> stockPosition = new StockPosition<>(ticker);
            stockPositions.add(stockPosition);

            return stockPosition;
        });

        position.addTransaction(transaction);
    }

    @Override
    public void addPositionTransaction(PositionTransaction transaction) {
        putTransaction(transaction);

        super.writeTransaction(transaction);
    }

    @Override
    protected PositionTransaction getObjectFromJSON(JSONObject json) {
        return new PositionTransaction(
            json.getString("ticker"),
            json.getInt("quantity"),
            json.getDouble("price"),
            json.getEnum(TradingSide.class, "side"),
            Date.from(Instant.ofEpochMilli(json.getLong("transactionDate")))
        );
    }

    @Override
    protected JSONObject getJSONFromObject(PositionTransaction transaction) {
        JSONObject json = new JSONObject();
        json.put("ticker", transaction.ticker());
        json.put("quantity", transaction.quantity());
        json.put("price", transaction.price());
        json.put("side", transaction.side().toString());
        json.put("transactionDate", transaction.transactionDate().toInstant().toEpochMilli());

        return json;
    }
}
