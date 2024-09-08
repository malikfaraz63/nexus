package com.nexus.atp.algos.congress;

import com.nexus.atp.common.transaction.BaseTransaction;
import com.nexus.atp.common.transaction.TradingSide;
import java.util.Date;

public class CongressTransaction extends BaseTransaction {
    private final String congressId;
    private final Date reportingDate;

    public CongressTransaction(String ticker,
                               String congressId,
                               int quantity,
                               double price,
                               TradingSide side,
                               Date transactionDate,
                               Date reportingDate) {
        super(ticker, quantity, price, side, transactionDate);
        this.congressId = congressId;
        this.reportingDate = reportingDate;
    }

    public String congressId() {
        return congressId;
    }

    public Date reportingDate() {
        return reportingDate;
    }

    @Override
    public <TRANSACTION extends BaseTransaction> TRANSACTION withQuantity(int quantity) {
        return (TRANSACTION) new CongressTransaction(
                ticker(),
                congressId(),
                quantity,
                price(),
                side(),
                transactionDate(),
                reportingDate()
        );
    }
}
