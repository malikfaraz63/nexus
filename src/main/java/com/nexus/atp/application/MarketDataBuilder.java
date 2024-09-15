package com.nexus.atp.application;

import com.nexus.atp.marketdata.MarketDataConfig;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.marketdata.manager.MarketDataManager;
import com.nexus.atp.marketdata.manager.MarketDataStorageManager;

public class MarketDataBuilder {
    private MarketDataConfig marketDataConfig;
    private final String marketDataFilePath;

    private MarketDataManager marketDataManager;

    public MarketDataBuilder(String marketDataFilePath) {
        this.marketDataFilePath = marketDataFilePath;
    }

    public void setMarketDataConfig(MarketDataConfig marketDataConfig) {
        this.marketDataConfig = marketDataConfig;
    }

    public MarketDataFetcher getMarketDataFetcher() {
        throw new UnsupportedOperationException(); // TODO: implement fetcher
    }

    public MarketDataManager getMarketDataManager() {
        if (marketDataManager == null) {
            marketDataManager = new MarketDataStorageManager(
                    getMarketDataFetcher(),
                    marketDataConfig,
                    marketDataFilePath
            );
        }

        return marketDataManager;
    }
}
