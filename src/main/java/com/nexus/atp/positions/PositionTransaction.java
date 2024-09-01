package com.nexus.atp.positions;

import com.nexus.atp.common.BaseTransaction;
import com.nexus.atp.common.TradingSide;
import java.util.Date;

public class PositionTransaction extends BaseTransaction {
    public PositionTransaction(
        String ticker,
        int quantity,
        double price,
        TradingSide side,
        Date transactionDate
    ) {
        super(ticker, quantity, price, side, transactionDate);
    }
}
