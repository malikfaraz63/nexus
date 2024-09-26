package com.nexus.atp.application;

import com.nexus.atp.marketdata.MarketDataConfig;
import com.nexus.atp.marketdata.api.AlphaVantageApiFetcher;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.marketdata.manager.MarketDataManager;
import com.nexus.atp.marketdata.manager.MarketDataStorageManager;

public class MarketDataBuilder {
    private MarketDataConfig marketDataConfig;
    private final String marketDataFilePath;

    private MarketDataManager marketDataManager;
    private MarketDataFetcher marketDataFetcher;

    private final ResourcesBuilder resourcesBuilder;
    private final String alphaVantageApiKey;

    public MarketDataBuilder(String marketDataFilePath, String alphaVantageApiKey, ResourcesBuilder resourcesBuilder) {
        this.marketDataFilePath = marketDataFilePath;
        this.alphaVantageApiKey = alphaVantageApiKey;
        this.resourcesBuilder = resourcesBuilder;
    }

    public void setMarketDataConfig(MarketDataConfig marketDataConfig) {
        this.marketDataConfig = marketDataConfig;
    }

    public MarketDataFetcher getMarketDataFetcher() {
        if (marketDataFetcher == null) {
            marketDataFetcher = new AlphaVantageApiFetcher(
                    alphaVantageApiKey,
                    resourcesBuilder.getScheduledTimer(),
                    resourcesBuilder.getLogger()

            );
        }
        return marketDataFetcher;
    }

    public MarketDataManager getMarketDataManager() {
        if (marketDataManager == null) {
            marketDataManager = new MarketDataStorageManager(
                    getMarketDataFetcher(),
                    marketDataConfig,
                    marketDataFilePath,
                resourcesBuilder.getLogger()
            );
        }

        return marketDataManager;
    }
}
