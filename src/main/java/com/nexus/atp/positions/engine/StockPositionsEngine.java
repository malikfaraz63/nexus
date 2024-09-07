package com.nexus.atp.positions.engine;

import com.nexus.atp.account.AccountManager;
import com.nexus.atp.algos.common.StockHoldUnitAllocation;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.common.transaction.TradingSide;
import com.nexus.atp.common.stock.StockTrade;
import com.nexus.atp.gateway.StockTradesGateway;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.marketdata.quote.StockQuote;
import com.nexus.atp.positions.PositionTransaction;
import com.nexus.atp.positions.controls.StockPositionControls;
import com.nexus.atp.positions.controls.StockPositionHoldOutcome;
import com.nexus.atp.positions.controls.StockPositionHoldType;
import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.HoldDecisionSubscriber;
import com.nexus.atp.positions.hold.StocksHold;
import com.nexus.atp.positions.manager.StockPositionsManager;
import com.nexus.atp.positions.validator.TradeDecisionValidator;

import java.util.Map;

public class StockPositionsEngine implements HoldDecisionSubscriber {
    private final StockPositionsManager stockPositionsManager;
    private final AccountManager accountManager;
    private final MarketDataFetcher marketDataFetcher;
    private final StockPositionControls stockPositionControls;
    private final TradeDecisionValidator tradeDecisionValidator;
    private final StockTradesGateway stockTradesGateway;
    private final StockPositionsEngineConfig engineConfig;

    private HoldDecision decision;

    public StockPositionsEngine(StockPositionsManager stockPositionsManager,
                                AccountManager accountManager,
                                MarketDataFetcher marketDataFetcher,
                                StockPositionControls stockPositionControls,
                                TradeDecisionValidator tradeDecisionValidator,
                                StockTradesGateway stockTradesGateway,
                                StockPositionsEngineConfig engineConfig) {
        this.stockPositionsManager = stockPositionsManager;
        this.accountManager = accountManager;
        this.marketDataFetcher = marketDataFetcher;
        this.stockPositionControls = stockPositionControls;
        this.tradeDecisionValidator = tradeDecisionValidator;
        this.stockTradesGateway = stockTradesGateway;
        this.engineConfig = engineConfig;

        initializeMarketDataSubscriptions();
    }

    private void initializeMarketDataSubscriptions() {
        stockPositionsManager
                .getStockPositions()
                .stream()
                .filter(StockPosition::hasOutstandingQuantity)
                .map(StockPosition::getTicker)
                .forEach(this::subscribeToStockTicker);
    }

    private void subscribeToStockTicker(String ticker) {
        marketDataFetcher.subscribeToStock(
                ticker,
                engineConfig.getMarketDataStartTime(),
                engineConfig.getMarketDataCallbackPeriod(), engineConfig.getMarketDataPeriodUnit(),
                this::onStockQuote);
    }

    @Override
    public void notifyNewHoldDecision(HoldDecision decision) {
        this.decision = decision;
        marketDataFetcher.getStockQuotes(decision.getTickers(), this::onQuotesForHoldDecision);
    }

    private void onQuotesForHoldDecision(Map<String, StockQuote> stockQuotes) {
        for (StocksHold stocksHold : decision.getStocksHolds()) {
            StockHoldUnitAllocation unitAllocation = stocksHold.getUnitAllocation();
            for (String ticker : stocksHold.getTickers()) {
                StockQuote quote = stockQuotes.get(ticker);

                int buyQuantity = accountManager.getStockPositionAllocation(quote, unitAllocation);
                boolean canAllocatePosition = buyQuantity > 0;

                if (canAllocatePosition) {
                    StockTrade stockTrade = new StockTrade(ticker, buyQuantity, TradingSide.BUY);

                    if (tradeDecisionValidator.isValidStockTrade(stockTrade)) {
                        stockTradesGateway.attemptTrade(stockTrade, this::onTradeOutcome);
                    }
                }
            }
        }
    }

    private void onStockQuote(StockQuote stockQuote) {
        StockPosition<PositionTransaction> position = stockPositionsManager.getStockPosition(stockQuote.getTicker());

        StockPositionHoldOutcome holdOutcome = stockPositionControls.getStockPositionHoldOutcome(position, stockQuote);

        if (holdOutcome.type() == StockPositionHoldType.HOLD) {
            return;
        }

        if (holdOutcome.type() == StockPositionHoldType.SELL) {
            int sellQuantity = holdOutcome.sellQuantity();
            assert sellQuantity <= position.getOutstandingQuantity();

            StockTrade stockTrade = new StockTrade(position.getTicker(), sellQuantity, TradingSide.SELL);

            if (tradeDecisionValidator.isValidStockTrade(stockTrade)) {
                stockTradesGateway.attemptTrade(stockTrade, this::onTradeOutcome);
            }
        }
    }

    private void onTradeOutcome(PositionTransaction transaction) {
        stockPositionsManager.addPositionTransaction(transaction);
        accountManager.updatePositionTransaction(transaction);
    }
}
