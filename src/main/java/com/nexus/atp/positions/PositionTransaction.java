package com.nexus.atp.positions;

import com.nexus.atp.common.transaction.BaseTransaction;
import com.nexus.atp.common.transaction.TradingSide;
import java.util.Date;

public class PositionTransaction extends BaseTransaction {
    public PositionTransaction(String ticker,
                               int quantity,
                               double price,
                               TradingSide side,
                               Date transactionDate) {
        super(ticker, quantity, price, side, transactionDate);
    }

    @Override
    public <TRANSACTION extends BaseTransaction> TRANSACTION withQuantity(int quantity) {
        return (TRANSACTION) new PositionTransaction(
                ticker(),
                quantity,
                price(),
                side(),
                transactionDate()
        );
    }
}
