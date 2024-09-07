package com.nexus.atp.positions.validator;

import com.nexus.atp.common.stock.StockTrade;

public interface TradeDecisionValidator {
    boolean isValidStockTrade(StockTrade stockTrade);
}
