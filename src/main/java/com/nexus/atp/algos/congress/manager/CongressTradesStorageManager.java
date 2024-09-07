package com.nexus.atp.algos.congress.manager;

import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.common.transaction.BaseTransactionStorageManager;
import com.nexus.atp.common.transaction.TradingSide;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class CongressTradesStorageManager
    extends BaseTransactionStorageManager<CongressTransaction> implements CongressPositionsManager {

    private final Map<String, CongressPosition> congressIdToPosition;
    private final Set<CongressPosition> congressPositions;

    private List<CongressTransaction> transactions;

    public CongressTradesStorageManager(String filePath) {
        super(filePath, "congressTransactions");
        this.congressIdToPosition = new HashMap<>();
        this.congressPositions = new HashSet<>();

        initializeTransactions();
    }

    private void initializeTransactions() {
        this.transactions = super.getTransactions();

        for (CongressTransaction transaction : transactions) {
            putTransaction(transaction);
        }

        congressPositions.forEach(CongressPosition::didViewUpdate);
    }

    @Override
    public Map<String, CongressPosition> getAllCongressPositions() {
        return congressIdToPosition;
    }

    @Override
    public Set<CongressPosition> getNewCongressPositions() {
        Set<CongressPosition> updatedPositions = congressPositions
                .stream()
                .filter(CongressPosition::wasUpdated)
                .collect(Collectors.toSet());

        updatedPositions.forEach(CongressPosition::didViewUpdate);

        return updatedPositions;
    }

    @Override
    public CongressPosition getCongressPosition(String congressId) {
        return congressIdToPosition.get(congressId);
    }

    private void putTransaction(CongressTransaction transaction) {
        CongressPosition position = congressIdToPosition.computeIfAbsent(transaction.congressId(), congressId -> {
            CongressPosition congressPosition = new CongressPosition(congressId);
            congressPositions.add(congressPosition);

            return congressPosition;
        });

        position.addTransaction(transaction);
    }

    @Override
    public void addCongressTransaction(CongressTransaction transaction) {
        putTransaction(transaction);

        super.writeTransaction(transaction);
    }

    @Override
    protected CongressTransaction getObjectFromJSON(JSONObject json) {
        return new CongressTransaction(
            json.getString("ticker"),
            json.getString("congressId"),
            json.getInt("quantity"),
            json.getDouble("price"),
            json.getEnum(TradingSide.class, "side"),
            Date.from(Instant.ofEpochMilli(json.getLong("transactionDate"))),
            Date.from(Instant.ofEpochMilli(json.getLong("reportingDate")))
        );
    }

    @Override
    protected JSONObject getJSONFromObject(CongressTransaction transaction) {
        JSONObject json = new JSONObject();
        json.put("ticker", transaction.ticker());
        json.put("congressId", transaction.congressId());
        json.put("quantity", transaction.quantity());
        json.put("price", transaction.price());
        json.put("side", transaction.side().toString());
        json.put("transactionDate", transaction.transactionDate().toInstant().toEpochMilli());
        json.put("reportingDate", transaction.reportingDate().toInstant().toEpochMilli());

        return json;
    }
}
