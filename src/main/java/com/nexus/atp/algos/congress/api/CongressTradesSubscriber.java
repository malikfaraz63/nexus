package com.nexus.atp.algos.congress.api;

import com.nexus.atp.algos.congress.CongressTransaction;
import java.util.List;

public interface CongressTradesSubscriber {
    void onNewTransactions(List<CongressTransaction> transactions);
}