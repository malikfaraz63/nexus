package com.nexus.atp.common.stock;

import com.nexus.atp.common.transaction.TradingSide;

public class StockTrade {
    private final String ticker;
    private final int quantity;
    private final TradingSide side;

    public StockTrade(String ticker, int quantity, TradingSide side) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.side = side;
    }

    public String getTicker() {
        return ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public TradingSide getSide() {
        return side;
    }
}
