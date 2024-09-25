package com.nexus.atp.common.stock;

import com.nexus.atp.common.transaction.TradingSide;
import com.nexus.atp.positions.PositionTransaction;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StockPositionTest {
    public static final String TICKER = "AAPL";
    public static final Date TRANSACTION_DATE = new Date(1725714139150L);

    /// MARK: Outstanding Buys

    public static final List<PositionTransaction> BASIC_TRANSACTIONS = List.of(
            new PositionTransaction(TICKER, 800, 152.75, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 800, 158.75, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 600, 154.00, TradingSide.BUY, TRANSACTION_DATE)
    );
    
    public static final List<PositionTransaction> PARTIAL_OUTSTANDING_TRANSACTIONS = List.of(
            // First 2 BUYs
            new PositionTransaction(TICKER, 1000, 150.25, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 151.00, TradingSide.BUY, TRANSACTION_DATE),

            // 3 SELLS that bring outstanding quantity to 0
            new PositionTransaction(TICKER, 800, 152.75, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 400, 153.00, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 300, 153.50, TradingSide.SELL, TRANSACTION_DATE),

            // BUY, SELL, BUY, SELL, BUY sequence
            new PositionTransaction(TICKER, 600, 154.00, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 300, 154.50, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 400, 155.00, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 250, 155.25, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 156.00, TradingSide.BUY, TRANSACTION_DATE)
    );

    private static final List<PositionTransaction> SHORT_POSITION = List.of(
            new PositionTransaction(TICKER, 500, 151.00, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 800, 152.75, TradingSide.SELL, TRANSACTION_DATE)
    );
    
    @Test
    public void stockPositionCorrectlyReturnsOutstandingBuys() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);

        BASIC_TRANSACTIONS.forEach(stockPosition::addTransaction);

        List<PositionTransaction> outstandingBuys = stockPosition.getOutstandingBuyTransactions();

        assertEquals(1, outstandingBuys.size());
        assertEquals(BASIC_TRANSACTIONS.getLast(), outstandingBuys.getFirst());
    }

    @Test
    public void stockPositionCorrectlyResolvesPartiallyOutstandingBuys() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);

        PARTIAL_OUTSTANDING_TRANSACTIONS.forEach(stockPosition::addTransaction);

        List<PositionTransaction> outstandingBuys = stockPosition.getOutstandingBuyTransactions();

        assertEquals(3, outstandingBuys.size());
        assertEquals(50, outstandingBuys.getFirst().quantity());
        assertEquals(500, outstandingBuys.getLast().quantity());
        assertEquals(PARTIAL_OUTSTANDING_TRANSACTIONS.getLast(), outstandingBuys.getLast());
    }

    @Test
    public void stockPositionResolvesOutstandingBuysForShortPosition() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);

        SHORT_POSITION.forEach(stockPosition::addTransaction);

        List<PositionTransaction> outstandingBuys = stockPosition.getOutstandingBuyTransactions();
        assertTrue(outstandingBuys.isEmpty());
    }

    /// MARK: Core Profitability

    private static final Date FROM = Date.from(TRANSACTION_DATE.toInstant().minusSeconds(10));
    private static final Date TO = Date.from(TRANSACTION_DATE.toInstant().plusSeconds(10));

    private static final List<PositionTransaction> PROFIT_TRANSACTIONS = List.of(
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 180, TradingSide.SELL, TRANSACTION_DATE)
    );

    private static final List<PositionTransaction> LOSS_TRANSACTIONS = List.of(
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 120, TradingSide.SELL, TRANSACTION_DATE)
    );

    private static final List<PositionTransaction> BURIED_PROFIT_TRANSACTIONS = List.of(
            new PositionTransaction(TICKER, 500, 150, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 180, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE)
    );

    private static final List<PositionTransaction> LEADING_SELLS_TRAILING_BUYS_ONLY = List.of(
            new PositionTransaction(TICKER, 500, 150, TradingSide.SELL, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE)
    );

    @Test
    public void stockPositionAdjustsCoreProfitabilityForProfit() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);
        PROFIT_TRANSACTIONS.forEach(stockPosition::addTransaction);

        assertEquals((180.0 - 150.0) / 150.0, stockPosition.getCoreProfitability(FROM, TO));
    }

    @Test
    public void stockPositionAdjustsCoreProfitabilityForLoss() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);
        LOSS_TRANSACTIONS.forEach(stockPosition::addTransaction);

        assertEquals(-(150.0 - 120.0) / 150.0, stockPosition.getCoreProfitability(FROM, TO));
    }

    @Test
    public void stockPositionIgnoresLeadingSellsAndTrailingBuys() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);
        BURIED_PROFIT_TRANSACTIONS.forEach(stockPosition::addTransaction);

        assertEquals((180.0 - 150.0) / 150.0, stockPosition.getCoreProfitability(FROM, TO));
    }

    @Test
    public void stockPositionHandlesEmptyTransactions() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);
        LEADING_SELLS_TRAILING_BUYS_ONLY.forEach(stockPosition::addTransaction);

        assertEquals(0, stockPosition.getCoreProfitability(TO, TO));
    }

    private static final List<PositionTransaction> STUPID_SCENARIO = List.of(
            new PositionTransaction(TICKER, 500, 150, TradingSide.BUY, TRANSACTION_DATE),
            new PositionTransaction(TICKER, 50, 180, TradingSide.SELL, TRANSACTION_DATE)
    );
    @Test
    public void stockPositionDoesNotAccomodateForNonSensicalScenarios() {
        // TODO: need to accomodate for this
        // I suppose this scenario is unlikely because the window will be large enough to accommodate for this?
        // maybe use date limit with excluding from outstanding buys?

        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(TICKER);
        STUPID_SCENARIO.forEach(stockPosition::addTransaction);

        System.out.println(stockPosition.getCoreProfitability(FROM, TO));
    }
}