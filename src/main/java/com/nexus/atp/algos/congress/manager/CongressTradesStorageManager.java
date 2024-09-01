package com.nexus.atp.algos.congress.manager;

import com.nexus.atp.algos.congress.CongressPosition;
import com.nexus.atp.algos.congress.CongressTransaction;
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

public class CongressTradesStorageManager
    extends BaseTransactionStorageManager<CongressTransaction> implements CongressPositionsManager {

    private List<CongressTransaction> transactions;

    private final Map<String, CongressPosition> congressIdToTransaction;
    private final Set<CongressPosition> congressPositions;

    public CongressTradesStorageManager(String filePath) {
        super(filePath, "congressTransactions");
        this.congressIdToTransaction = new HashMap<>();
        this.congressPositions = new HashSet<>();
    }

    @Override
    public Set<CongressPosition> getCongressPositions() {
        if (transactions == null) {
            this.transactions = super.getTransactions();

            for (CongressTransaction transaction : transactions) {
                putTransaction(transaction);
            }
        }

        return congressPositions;
    }

    @Override
    public CongressPosition getCongressPosition(String congressId) {
        return congressIdToTransaction.get(congressId);
    }

    private void putTransaction(CongressTransaction transaction) {
        CongressPosition position = congressIdToTransaction.computeIfAbsent(transaction.congressId(), congressId -> {
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
