package com.nexus.atp.positions.engine;

import com.nexus.atp.positions.hold.HoldDecision;
import com.nexus.atp.positions.hold.HoldDecisionSubscriber;
import com.nexus.atp.positions.manager.StockPositionsManager;

public class StockPositionsEngine implements HoldDecisionSubscriber {
    private final StockPositionsManager stockPositionsManager;

    public StockPositionsEngine(StockPositionsManager stockPositionsManager) {
        this.stockPositionsManager = stockPositionsManager;
    }

    @Override
    public void notifyNewHoldDecision(HoldDecision decision) {

    }
}
