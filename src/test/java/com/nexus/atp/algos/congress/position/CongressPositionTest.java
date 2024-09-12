package com.nexus.atp.algos.congress.position;

import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.common.transaction.TradingSide;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CongressPositionTest {
    private static final String CONGRESS_ID = "M000355";
    private static final Date TRANSACTION_DATE = new Date(1725714139156L);
    private static final Date REPORTING_DATE = new Date(1725783747195L);

    private static final List<CongressTransaction> CONGRESS_TRANSACTIONS = List.of(
            new CongressTransaction(
                    "LMT",
                    CONGRESS_ID,
                    1000,
                    447.11,
                    TradingSide.BUY,
                    TRANSACTION_DATE,
                    REPORTING_DATE
            ),
            new CongressTransaction(
                    "AAPL",
                    "M000355",
                    500,
                    174.65,
                    TradingSide.SELL,
                    TRANSACTION_DATE,
                    REPORTING_DATE
            ),
            new CongressTransaction(
                    "TSLA",
                    "M000355",
                    200,
                    670.50,
                    TradingSide.BUY,
                    TRANSACTION_DATE,
                    REPORTING_DATE
            )
    );

    private static final CongressTransaction UPDATE_TRANSACTION = new CongressTransaction(
            "AAPL",
            "M000355",
            150,
            3223.72,
            TradingSide.SELL,
            TRANSACTION_DATE,
            REPORTING_DATE
    );

    private static final CongressTransaction OTHER_TRANSACTION = new CongressTransaction(
            "LMT",
            "M000356",
            1000,
            447.11,
            TradingSide.BUY,
            TRANSACTION_DATE,
            REPORTING_DATE
    );

    @Test(expected = IllegalArgumentException.class)
    public void congressPositionRejectsTransactionsForDifferentId() {
        CongressPosition position = new CongressPosition(CONGRESS_ID);

        position.addTransaction(OTHER_TRANSACTION);
    }

    @Test
    public void congressPositionOnlyReturnsUpdatedPositions() {
        CongressPosition position = new CongressPosition(CONGRESS_ID);

        assertFalse(position.wasUpdated());

        position.addTransaction(UPDATE_TRANSACTION);
        assertTrue(position.wasUpdated());

        position.didViewUpdate();
        assertFalse(position.wasUpdated());
    }

    @Test
    public void congressPositionReturnsCorrectQuantity() {
        CongressPosition position = new CongressPosition(CONGRESS_ID);

        CONGRESS_TRANSACTIONS.forEach(position::addTransaction);

        Date fromDate = new Date(1725714139150L);
        Date toDate = new Date(1725714139170L);

        int quantity = CONGRESS_TRANSACTIONS
                .stream()
                .map(CongressTransaction::quantity)
                .reduce(0, Integer::sum);
        assertEquals(quantity, position.getQuantity(fromDate, toDate));
    }
}