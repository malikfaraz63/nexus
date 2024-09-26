package com.nexus.atp.algos.congress.api;

import com.nexus.atp.algos.congress.CongressTransaction;
import java.util.List;

public interface CongressTradesFetcher {
    void getHistoricCongressTrades(CongressTradesHandler<List<CongressTransaction>> handler);
    void subscribe(CongressTransactionsSubscriber subscriber);
}
