package com.nexus.atp.positions.controls;

import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.positions.PositionTransaction;

public interface StockPositionControls {
    StockPositionHoldOutcome getStockPositionHoldOutcome(StockPosition<PositionTransaction> stockPosition,
                                                         StockQuoteIntraDay stockQuote);
}
