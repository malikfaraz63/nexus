package com.nexus.atp.positions.controls;

import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.common.stock.StockPositionTest;
import com.nexus.atp.common.transaction.TradingSide;
import com.nexus.atp.marketdata.manager.MarketDataManager;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.positions.PositionTransaction;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class StockPositionControlsRunnerTest {
    private static final double TAKE_PROFIT = 0.6;
    private static final double STOP_LOSS = 0.2;

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private final EngineTimeProvider engineTimeProvider = context.mock(EngineTimeProvider.class);
    private final MarketDataManager marketDataManager = context.mock(MarketDataManager.class);
    
    private final StockPositionControlsConfig controlsConfig = new StockPositionControlsConfig(TAKE_PROFIT, STOP_LOSS);
    
    private final StockPositionControlsRunner controlsRunner = new StockPositionControlsRunner(
            controlsConfig,
            marketDataManager,
            engineTimeProvider);

    @Test
    public void controlsSellStockWhenLossThresholdMet() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(StockPositionTest.TICKER);

        double stockPrice = 150.0;
        Date buyDate = StockPositionTest.TRANSACTION_DATE;
        PositionTransaction transaction = new PositionTransaction(StockPositionTest.TICKER, 1, 150.0, TradingSide.BUY, buyDate);
        stockPosition.addTransaction(transaction);

        Date currentDate = Date.from(Instant.ofEpochMilli(1727191642349L));
        StockQuoteIntraDay stockQuote = new StockQuoteIntraDay(StockPositionTest.TICKER, (1 - STOP_LOSS) * stockPrice, currentDate);

        context.checking(new Expectations() {{
            oneOf(engineTimeProvider).getEngineTime();
            will(returnValue(currentDate));

            oneOf(marketDataManager).getMaxPriceForStock(StockPositionTest.TICKER, buyDate, currentDate);
            will(returnValue(stockPrice));
        }});

        StockPositionHoldOutcome outcome = controlsRunner.getStockPositionHoldOutcome(stockPosition, stockQuote);

        assertEquals(StockPositionHoldType.SELL, outcome.type());
        assertEquals(StockPositionSellReason.STOP_LOSS, outcome.sellReason());
        assertEquals(1, outcome.sellQuantity());
    }

    @Test
    public void controlSellsCorrectOutstandingQuantityWhenLossThresholdMet() {
        StockPosition<PositionTransaction> stockPosition = new StockPosition<>(StockPositionTest.TICKER);
        StockPositionTest.PARTIAL_OUTSTANDING_TRANSACTIONS.forEach(stockPosition::addTransaction);

        double maxPrice = 153.65;
        Date currentDate = Date.from(Instant.ofEpochMilli(1727191642349L));
        StockQuoteIntraDay stockQuote = new StockQuoteIntraDay(StockPositionTest.TICKER, (1 - STOP_LOSS) * maxPrice - 0.01, currentDate);

        context.checking(new Expectations() {{
            oneOf(engineTimeProvider).getEngineTime();
            will(returnValue(currentDate));

            oneOf(marketDataManager).getMaxPriceForStock(StockPositionTest.TICKER, StockPositionTest.TRANSACTION_DATE, currentDate);
            will(returnValue(maxPrice));
        }});

        StockPositionHoldOutcome outcome = controlsRunner.getStockPositionHoldOutcome(stockPosition, stockQuote);

        assertEquals(StockPositionHoldType.SELL, outcome.type());
        assertEquals(StockPositionSellReason.STOP_LOSS, outcome.sellReason());
        assertEquals(950, outcome.sellQuantity());
    }
}