package com.nexus.atp.positions.engine;

import com.nexus.atp.account.AccountManager;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.marketdata.MarketDataFetcher;
import com.nexus.atp.marketdata.quote.StockQuote;
import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.HoldDecisionSubscriber;
import com.nexus.atp.positions.hold.StocksHold;
import com.nexus.atp.positions.manager.StockPositionsManager;

import java.util.Map;

public class StockPositionsEngine implements HoldDecisionSubscriber {
    private final StockPositionsManager stockPositionsManager;
    private final AccountManager accountManager;
    private final MarketDataFetcher marketDataFetcher;
    private final StockPositionsEngineConfig engineConfig;

    private HoldDecision decision;

    public StockPositionsEngine(StockPositionsManager stockPositionsManager,
                                AccountManager accountManager,
                                MarketDataFetcher marketDataFetcher,
                                StockPositionsEngineConfig engineConfig) {
        this.stockPositionsManager = stockPositionsManager;
        this.accountManager = accountManager;
        this.marketDataFetcher = marketDataFetcher;
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
            for (String ticker : stocksHold.getTickers()) {
                StockQuote quote = stockQuotes.get(ticker);

                // TODO: continue allocation with quote price
            }
        }
    }

    private void onStockQuote(StockQuote stockQuote) {

    }
}
