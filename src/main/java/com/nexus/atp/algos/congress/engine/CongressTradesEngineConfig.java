package com.nexus.atp.algos.congress.engine;

import com.nexus.atp.algos.common.AllocationConfig;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CongressTradesEngineConfig {
    private final CongressTradesEngineSetting setting;
    private final Duration tradesEvaluationInterval;

    private final List<String> congressIds;
    private int congressLimit = INVALID_LIMIT;
    private int stockTradeLimit = INVALID_LIMIT;
    private AllocationConfig allocationConfig;
    private final LocalTime congressTradesLoadTime;

    private static final int INVALID_LIMIT = -1;

    public CongressTradesEngineConfig(CongressTradesEngineSetting setting,
                                      Duration tradesEvaluationInterval,
                                      LocalTime congressTradesLoadTime) {
        this.setting = setting;
        this.tradesEvaluationInterval = tradesEvaluationInterval;
        this.congressTradesLoadTime = congressTradesLoadTime;

        this.congressIds = new ArrayList<>();
    }

    public CongressTradesEngineSetting getSetting() {
        return setting;
    }

    public Duration getTradesEvaluationInterval() {
        return tradesEvaluationInterval;
    }

    public void addCongressId(String congressId) {
        if (setting == CongressTradesEngineSetting.AUTO_RANKED) {
            throw new UnsupportedOperationException("Cannot set congressId for auto-ranked setting");
        }
        
        congressIds.add(congressId);
    }

    public List<String> getCongressIds() {
        if (setting == CongressTradesEngineSetting.AUTO_RANKED) {
            throw new UnsupportedOperationException("Cannot get congressIds for auto-ranked setting");
        }

        return congressIds;
    }

    public void setCongressLimit(int congressLimit) {
        this.congressLimit = congressLimit;
    }

    public long getCongressLimit() {
        if (setting == CongressTradesEngineSetting.MANUAL_RANKED) {
            throw new UnsupportedOperationException("Cannot get congressLimit for manual-ranked setting");
        }

        if (congressLimit == INVALID_LIMIT) {
            throw new IllegalStateException("Congress limit was not set");
        }

        return congressLimit;
    }

    public void setStockTradeLimit(int stockTradeLimit) {
        this.stockTradeLimit = stockTradeLimit;
    }

    public int getStockTradeLimit() {
        if (setting == CongressTradesEngineSetting.MANUAL_RANKED) {
            throw new UnsupportedOperationException("Cannot get stockLimit for manual-ranked setting");
        }
        if (stockTradeLimit == INVALID_LIMIT) {
            throw new IllegalStateException("Stock trade limit was not set");
        }

        return stockTradeLimit;
    }

    public void setAlgoHoldAllocationConfig(AllocationConfig allocationConfig) {
        this.allocationConfig = allocationConfig;
    }

    public AllocationConfig getAlgoHoldAllocationConfig() {
        if (setting == CongressTradesEngineSetting.MANUAL_RANKED) {
            throw new UnsupportedOperationException("Cannot get allocation config for manual-ranked setting");
        }
        if (allocationConfig == null) {
            throw new IllegalStateException("Allocation config was not set");
        }

        return allocationConfig;
    }

    public LocalTime getCongressTradesLoadTime() {
        return congressTradesLoadTime;
    }
}
