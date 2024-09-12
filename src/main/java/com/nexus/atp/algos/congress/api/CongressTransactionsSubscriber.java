package com.nexus.atp.algos.congress.api;

import com.nexus.atp.algos.congress.CongressTransaction;
import java.util.List;

public interface CongressTransactionsSubscriber {
    void onNewTransactions(List<CongressTransaction> transactions);
}