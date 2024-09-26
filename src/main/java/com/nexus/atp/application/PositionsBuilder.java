package com.nexus.atp.application;

import com.nexus.atp.account.AccountManager;
import com.nexus.atp.gateway.StockTradesGateway;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.positions.controls.StockPositionControls;
import com.nexus.atp.positions.controls.StockPositionControlsConfig;
import com.nexus.atp.positions.controls.StockPositionControlsRunner;
import com.nexus.atp.positions.engine.StockPositionsEngine;
import com.nexus.atp.positions.engine.StockPositionsEngineConfig;
import com.nexus.atp.positions.manager.StockPositionsManager;
import com.nexus.atp.positions.manager.StockPositionsStorageManager;
import com.nexus.atp.positions.validator.TradeDecisionValidator;

import java.time.LocalTime;

public class PositionsBuilder {
    private final String stockPositionsFilePath;
    private final MarketDataBuilder marketDataBuilder;
    private final ResourcesBuilder resourcesBuilder;

    public PositionsBuilder(String stockPositionsFilePath, MarketDataBuilder marketDataBuilder, ResourcesBuilder resourcesBuilder) {
        this.stockPositionsFilePath = stockPositionsFilePath;
        this.marketDataBuilder = marketDataBuilder;
        this.resourcesBuilder = resourcesBuilder;
    }

    public StockPositionsEngine getPositionsEngine() {
        return new StockPositionsEngine(
                getPositionsManager(),
                getAccountManager(),
                getMarketDataFetcher(),
                getStockPositionControls(),
                getTradeDecisionValidator(),
                getStockTradesGateway(),
                getPositionsEngineConfig()
        );
    }

    private StockPositionsManager getPositionsManager() {
        return new StockPositionsStorageManager(stockPositionsFilePath, resourcesBuilder.getLogger());
    }

    private AccountManager getAccountManager() {
        throw new UnsupportedOperationException(); // TODO: implement account manager
    }

    private MarketDataFetcher getMarketDataFetcher() {
        return marketDataBuilder.getMarketDataFetcher();
    }

    private StockPositionControls getStockPositionControls() {
        StockPositionControlsConfig controlsConfig = new StockPositionControlsConfig(
                0.2,
                0.2
        );

        return new StockPositionControlsRunner(
                controlsConfig,
                marketDataBuilder.getMarketDataManager(),
                resourcesBuilder.getTimeProvider()
        );
    }

    private TradeDecisionValidator getTradeDecisionValidator() {
        throw new UnsupportedOperationException(); // TODO: implement trade decision validator
    }

    private StockTradesGateway getStockTradesGateway() {
        throw new UnsupportedOperationException(); // TODO: implement stock trades gateway
    }

    public StockPositionsEngineConfig getPositionsEngineConfig() {
        return new StockPositionsEngineConfig(
                LocalTime.of(21, 0),
                LocalTime.of(6, 0),
                15
        );
    }
}
