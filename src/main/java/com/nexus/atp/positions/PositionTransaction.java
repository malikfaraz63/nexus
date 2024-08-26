package com.nexus.atp.positions;

import com.nexus.resources.TradingSide;
import java.util.Date;
import java.util.Objects;

public record PositionTransaction(String ticker,
                                  int quantity,
                                  double price,
                                  TradingSide side,
                                  Date transactionDate) implements Comparable<PositionTransaction> {

    @Override
    public int hashCode() {
        return Objects.hash(ticker, transactionDate);
    }

    @Override
    public int compareTo(PositionTransaction otherTransaction) {
        return transactionDate.compareTo(otherTransaction.transactionDate);
    }
}
