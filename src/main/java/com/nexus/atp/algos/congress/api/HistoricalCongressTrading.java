package com.nexus.atp.algos.congress.api;

import com.nexus.atp.common.transaction.TradingSide;

import java.util.Date;

public record HistoricalCongressTrading(String congressId,
                                        String ticker,
                                        Date transactionDate,
                                        Date reportingDate,
                                        double amount,
                                        TradingSide side) {
}
