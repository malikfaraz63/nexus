package com.nexus.atp.algos.congress.engine;

import com.nexus.atp.algos.common.AlgoHoldDecision;
import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.algos.congress.RankedCongress;
import com.nexus.atp.algos.congress.position.CongressPositionsSubscriber;
import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.stock.RankedStock;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.positions.hold.HoldDecisionSubscriber;

import java.util.*;

public class CongressTradesAlgoEngine implements CongressPositionsSubscriber {
    private final EngineTimeProvider engineTimeProvider;
    private final CongressTradesEngineConfig engineConfig;

    private final HoldDecisionSubscriber holdDecisionSubscriber;

    public CongressTradesAlgoEngine(EngineTimeProvider engineTimeProvider, CongressTradesEngineConfig engineConfig, HoldDecisionSubscriber holdDecisionSubscriber) {
        this.engineTimeProvider = engineTimeProvider;
        this.engineConfig = engineConfig;
        this.holdDecisionSubscriber = holdDecisionSubscriber;
    }

    @Override
    public void initializeCongressPositions(Map<String, CongressPosition> congressIdToPosition) {
        if (engineConfig.getSetting() == CongressTradesEngineSetting.AUTO_RANKED) {
            computeAutoRankedHold(congressIdToPosition);
        }
    }

    @Override
    public void updateNewCongressPositions(Set<CongressPosition> congressIdToPosition) {
        // TODO: update hold based on intra-day trades
    }

    private void computeAutoRankedHold(Map<String, CongressPosition> congressIdToPosition) {
        Date currentTime = engineTimeProvider.getEngineTime();
        Date lookbackTime = Date.from(currentTime.toInstant().minus(engineConfig.getTradesEvaluationInterval()));

        List<RankedCongress> rankedCongressList = new ArrayList<>();
        for (CongressPosition congressPosition : congressIdToPosition.values()) {
            double adjustedProfitability = congressPosition.getVolumeAdjustedProfitability(lookbackTime, currentTime);
            rankedCongressList.add(new RankedCongress(congressPosition.getCongressId(), adjustedProfitability));
        }

        rankedCongressList.sort(Comparator.reverseOrder());

        List<String> rankedCongressIds = rankedCongressList
                .stream()
                .limit(engineConfig.getCongressLimit())
                .map(RankedCongress::getCongressId)
                .toList();

        // rank stocks by volume traded

        Map<String, RankedStock> rankedStocks = new HashMap<>();
        for (CongressPosition congressPosition : congressIdToPosition.values()) {
            for (StockPosition<CongressTransaction> stockPosition : congressPosition.getStockPositions()) {
                RankedStock rankedStock = rankedStocks.computeIfAbsent(stockPosition.getTicker(), RankedStock::new);
                rankedStock.addTradedVolume(stockPosition.getVolume());
            }
        }

        List<RankedStock> rankedStockList = new ArrayList<>(rankedStocks.values().stream().toList());
        rankedStockList.sort(Comparator.reverseOrder());

        List<String> rankedStockTickers = rankedStockList
                .stream()
                .limit(engineConfig.getStockTradeLimit())
                .map(RankedStock::getTicker)
                .toList();

        AlgoHoldDecision holdDecision = new AlgoHoldDecision(engineConfig.getAlgoHoldAllocationConfig());

        for (String rankedCongressId : rankedCongressIds) {
            for (String rankedStockTicker : rankedStockTickers) {
                CongressPosition congressPosition = congressIdToPosition.get(rankedCongressId);
                StockPosition<CongressTransaction> nextStock = congressPosition.getStockPosition(rankedStockTicker);

                if (nextStock == null) {
                    continue;
                }
                if (!holdDecision.addStockTicker(nextStock.getTicker())) {
                    handleSaturatedDecision(holdDecision);
                    return;
                }
            }
        }

        handleSaturatedDecision(holdDecision);
    }

    private void handleSaturatedDecision(AlgoHoldDecision holdDecision) {
        holdDecisionSubscriber.notifyNewHoldDecision(holdDecision);
    }
}
