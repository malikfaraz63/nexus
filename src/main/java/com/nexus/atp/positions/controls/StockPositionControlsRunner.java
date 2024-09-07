package com.nexus.atp.positions.controls;

import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.marketdata.manager.MarketDataManager;
import com.nexus.atp.marketdata.quote.StockQuote;
import com.nexus.atp.positions.PositionTransaction;

import java.util.Date;
import java.util.List;

public class StockPositionControlsRunner implements StockPositionControls {
    private final StockPositionControlsConfig controlsConfig;
    private final MarketDataManager marketDataManager;
    private final EngineTimeProvider engineTimeProvider;

    private int sellQuantity = 0;

    public StockPositionControlsRunner(StockPositionControlsConfig controlsConfig,
                                       MarketDataManager marketDataManager,
                                       EngineTimeProvider engineTimeProvider) {
        this.controlsConfig = controlsConfig;
        this.marketDataManager = marketDataManager;
        this.engineTimeProvider = engineTimeProvider;
    }

    @Override
    public StockPositionHoldOutcome getStockPositionHoldOutcome(StockPosition<PositionTransaction> stockPosition,
                                                                StockQuote stockQuote) {
        assert stockPosition.getTicker().equals(stockQuote.getTicker());

        StockPositionHoldType takeProfitControlResult = runTakeProfitControl(stockPosition, stockQuote);
        if (takeProfitControlResult == StockPositionHoldType.SELL) {
            assert sellQuantity > 0;
            return new StockPositionHoldOutcome(takeProfitControlResult, sellQuantity, StockPositionSellReason.TAKE_PROFIT);
        }

        StockPositionHoldType stopLossControlResult = runTrailingStopLossControl(stockPosition, stockQuote);
        if (stopLossControlResult == StockPositionHoldType.SELL) {
            assert sellQuantity > 0;
            return new StockPositionHoldOutcome(stopLossControlResult, sellQuantity, StockPositionSellReason.STOP_LOSS);
        }

        return new StockPositionHoldOutcome(StockPositionHoldType.HOLD, 0);
    }

    /**
     * Runs whether to sell for profit from the current stock's outstanding buy position
     * @param stockPosition the position to get the outstanding buys from
     * @param stockQuote the latest price quote for the stock
     * @return whether to hold or sell
     */
    private StockPositionHoldType runTakeProfitControl(StockPosition<PositionTransaction> stockPosition,
                                         StockQuote stockQuote) {
        List<PositionTransaction> transactions = stockPosition.getOutstandingBuyTransactions();

        sellQuantity = 0;
        int i = 0;
        while (profitExceedsMargin(transactions.get(i), stockQuote)) {
            sellQuantity += transactions.get(i).quantity();
            i++;
        }

        if (sellQuantity == 0) {
            return StockPositionHoldType.HOLD;
        } else {
            return StockPositionHoldType.SELL;
        }
    }

    private boolean profitExceedsMargin(PositionTransaction transaction, StockQuote stockQuote) {
        double profitMargin = stockQuote.getPrice() / transaction.price();
        profitMargin--;

        return profitMargin >= controlsConfig.getTakeProfitMargin();
    }

    private StockPositionHoldType runTrailingStopLossControl(StockPosition<PositionTransaction> stockPosition,
                                                             StockQuote stockQuote) {
        List<PositionTransaction> transactions = stockPosition.getOutstandingBuyTransactions();
        Date earliestOutstandingBuyDate = transactions.getFirst().transactionDate();

        double maximumPrice = marketDataManager.getMaxPriceForStock(stockPosition.getTicker(),
                earliestOutstandingBuyDate, engineTimeProvider.getEngineTime());

        if (lossExceedsThreshold(maximumPrice, stockQuote)) {
            sellQuantity = transactions
                    .stream()
                    .map(PositionTransaction::quantity)
                    .reduce(0, Integer::sum);
            return StockPositionHoldType.SELL;
        } else {
            return StockPositionHoldType.HOLD;
        }
    }

    private boolean lossExceedsThreshold(double maximumPrice, StockQuote stockQuote) {
        double delta = stockQuote.getPrice() - maximumPrice;
        double lossMargin = delta / maximumPrice;

        return lossMargin >= controlsConfig.getStopLossMargin();
    }
}
