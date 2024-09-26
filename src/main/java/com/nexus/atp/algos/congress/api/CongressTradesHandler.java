package com.nexus.atp.algos.congress.api;

import java.util.Map;

public interface CongressTradesHandler<TRADE> {
    void onCongressTrades(Map<String, TRADE> congressTransactions);
}
