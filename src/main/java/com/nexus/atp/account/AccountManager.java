package com.nexus.atp.account;

import com.nexus.atp.algos.common.StockHoldUnitAllocation;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.positions.PositionTransaction;

public interface AccountManager {
    int getStockPositionAllocation(StockQuoteIntraDay stockQuote, StockHoldUnitAllocation unitAllocation);
    void updatePositionTransaction(PositionTransaction transaction);
}
