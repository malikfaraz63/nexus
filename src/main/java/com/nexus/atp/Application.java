package com.nexus.atp;

import com.nexus.atp.application.AlgoBuilder;
import com.nexus.atp.application.MarketDataBuilder;
import com.nexus.atp.application.PositionsBuilder;
import com.nexus.atp.application.ResourcesBuilder;

public class Application {
    public static void main(String[] args) {
        ResourcesBuilder resourcesBuilder = new ResourcesBuilder();

        MarketDataBuilder marketDataBuilder = new MarketDataBuilder(System.getenv("MARKET_DATA_PATH"));

        PositionsBuilder positionsBuilder = new PositionsBuilder(System.getenv("STOCK_POSITIONS_PATH"), marketDataBuilder, resourcesBuilder);
        marketDataBuilder.setMarketDataConfig(positionsBuilder.getPositionsEngineConfig());

        AlgoBuilder algoBuilder = new AlgoBuilder(resourcesBuilder, System.getenv("CONGRESS_TRADES_PATH"));
        algoBuilder.setupCongressTrades(positionsBuilder.getPositionsEngine());
    }
}
