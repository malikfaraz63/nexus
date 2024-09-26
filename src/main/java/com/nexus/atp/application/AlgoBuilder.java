package com.nexus.atp.application;

import com.nexus.atp.algos.common.AllocationConfig;
import com.nexus.atp.algos.common.StockHoldUnitAllocation;
import com.nexus.atp.algos.congress.CongressTransactionsClient;
import com.nexus.atp.algos.congress.api.CongressTradesFetcher;
import com.nexus.atp.algos.congress.api.QuiverQuantApiFetcher;
import com.nexus.atp.algos.congress.engine.CongressTradesAlgoEngine;
import com.nexus.atp.algos.congress.engine.CongressTradesEngineConfig;
import com.nexus.atp.algos.congress.engine.CongressTradesEngineSetting;
import com.nexus.atp.algos.congress.manager.CongressPositionsManager;
import com.nexus.atp.algos.congress.manager.CongressTradesStorageManager;
import com.nexus.atp.positions.hold.HoldDecisionSubscriber;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class AlgoBuilder {
    private final ResourcesBuilder resourcesBuilder;
    private final MarketDataBuilder marketDataBuilder;
    private final String congressTradesFilePath;
    private final String quiverQuantAuthToken;

    private CongressTradesFetcher congressTradesFetcher;

    public AlgoBuilder(ResourcesBuilder resourcesBuilder, MarketDataBuilder marketDataBuilder, String congressTradesFilePath, String quiverQuantAuthToken) {
        this.congressTradesFilePath = congressTradesFilePath;
        this.marketDataBuilder = marketDataBuilder;
        this.resourcesBuilder = resourcesBuilder;
        this.quiverQuantAuthToken = quiverQuantAuthToken;
    }

    private CongressTradesFetcher getCongressTradesFetcher() {
        if (congressTradesFetcher == null) {
            congressTradesFetcher = new QuiverQuantApiFetcher(
                quiverQuantAuthToken,
                resourcesBuilder.getLogger(),
                marketDataBuilder.getMarketDataManager(),
                resourcesBuilder.getTimeProvider(),
                resourcesBuilder.getScheduledTimer()
            );
        }

        return congressTradesFetcher;
    }

    private AllocationConfig getCongressHoldAllocationConfig() {
        return new AllocationConfig(
                Map.of(
                        StockHoldUnitAllocation.LARGE, 0.50,
                        StockHoldUnitAllocation.MEDIUM, 0.25,
                        StockHoldUnitAllocation.SMALL, 0.25
                ),
                List.of(
                        StockHoldUnitAllocation.LARGE,
                        StockHoldUnitAllocation.MEDIUM,
                        StockHoldUnitAllocation.SMALL
                )
        );
    }

    private CongressTradesEngineConfig getCongressTradesEngineConfig() {
        CongressTradesEngineConfig engineConfig = new CongressTradesEngineConfig(
                CongressTradesEngineSetting.AUTO_RANKED,
                Duration.of(3, ChronoUnit.MONTHS),
                LocalTime.of(6, 0)
        );
        engineConfig.setCongressLimit(10);
        engineConfig.setStockTradeLimit(20);
        engineConfig.setAlgoHoldAllocationConfig(getCongressHoldAllocationConfig());

        return engineConfig;
    }

    private CongressPositionsManager getCongressPositionsManager() {
        return new CongressTradesStorageManager(congressTradesFilePath, resourcesBuilder.getLogger(), getCongressTradesFetcher());
    }

    private CongressTradesAlgoEngine getCongressTradesAlgoEngine(HoldDecisionSubscriber holdDecisionSubscriber) {
        return new CongressTradesAlgoEngine(
                resourcesBuilder.getTimeProvider(),
                getCongressTradesEngineConfig(),
                holdDecisionSubscriber
        );
    }

    public void setupCongressTrades(HoldDecisionSubscriber holdDecisionSubscriber) {
        new CongressTransactionsClient(
                getCongressTradesEngineConfig(),
                getCongressTradesAlgoEngine(holdDecisionSubscriber),
                getCongressPositionsManager(),
                getCongressTradesFetcher(),
                resourcesBuilder.getScheduledTimer()
        );
    }
}
