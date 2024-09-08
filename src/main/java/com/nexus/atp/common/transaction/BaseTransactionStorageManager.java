package com.nexus.atp.common.transaction;

import java.util.ArrayList;

import com.nexus.atp.common.storage.BaseStorageManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public abstract class BaseTransactionStorageManager<TRANSACTION extends BaseTransaction> extends BaseStorageManager {
    private final String transactionsKey;

    public BaseTransactionStorageManager(String filePath, String transactionsKey) {
        super(filePath);
        this.transactionsKey = transactionsKey;
    }

    protected List<TRANSACTION> getTransactions() {
        JSONArray transactionsJson = fileContents.getJSONArray(transactionsKey);

        // Map the JSONObject to the Person record
        List<TRANSACTION> transactions = new ArrayList<>();
        for (int i = 0; i < transactionsJson.length(); i++) {
            JSONObject object = transactionsJson.getJSONObject(i);
            transactions.add(getObjectFromJSON(object));
        }

        return transactions;
    }

    protected void writeTransaction(TRANSACTION transaction) {
        JSONObject json = getJSONFromObject(transaction);

        JSONArray transactionsJson = fileContents.getJSONArray(transactionsKey);
        transactionsJson.put(json);

        writeFileContents();
    }

    protected abstract TRANSACTION getObjectFromJSON(JSONObject json);

    protected abstract JSONObject getJSONFromObject(TRANSACTION transaction);
}
