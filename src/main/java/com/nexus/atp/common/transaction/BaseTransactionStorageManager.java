package com.nexus.atp.common.transaction;

import java.nio.file.Path;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class BaseTransactionStorageManager<TRANSACTION extends BaseTransaction> {
    private final String filePath;
    private final String transactionsKey;

    private final Path path;
    private JSONObject fileContents;

    public BaseTransactionStorageManager(String filePath, String transactionsKey) {
        this.filePath = filePath;
        this.transactionsKey = transactionsKey;
        this.path = Paths.get(filePath);

        initialize();
    }

    private void initialize() {
        try {
            String fileContents = new String(Files.readAllBytes(path));
            this.fileContents = new JSONObject(fileContents);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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

        try {
            Files.write(Paths.get(filePath), fileContents.toString(2).getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    protected abstract TRANSACTION getObjectFromJSON(JSONObject json);

    protected abstract JSONObject getJSONFromObject(TRANSACTION transaction);
}
