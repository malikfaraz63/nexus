package com.nexus.atp.gateway;

import com.nexus.atp.common.stock.StockTrade;
import com.nexus.atp.common.transaction.TransactionHandler;
import com.nexus.atp.positions.PositionTransaction;

public interface StockTradesGateway {
    void attemptTrade(StockTrade stockTrade, TransactionHandler<PositionTransaction> successfulTransactionHandler);
}
