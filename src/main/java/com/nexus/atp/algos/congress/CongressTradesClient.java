package com.nexus.atp.algos.congress;

import com.nexus.atp.algos.common.AlgoSubscriber;
import com.nexus.atp.algos.congress.api.CongressTradesFetcher;
import com.nexus.atp.algos.congress.api.CongressTradesSubscriber;
import com.nexus.atp.algos.congress.manager.CongressPositionsManager;
import java.util.List;
import java.util.Set;

public class CongressTradesClient implements CongressTradesSubscriber {
    private final AlgoSubscriber algoSubscriber;
    private final CongressPositionsManager congressPositionsManager;

    public CongressTradesClient(
        AlgoSubscriber algoSubscriber,
        CongressPositionsManager congressPositionsManager,
        CongressTradesFetcher congressTradesFetcher
    ) {
        this.algoSubscriber = algoSubscriber;
        this.congressPositionsManager = congressPositionsManager;

        congressTradesFetcher.subscribe(this);
    }

    @Override
    public void onNewTransactions(List<CongressTransaction> transactions) {
        transactions.forEach(congressPositionsManager::addCongressTransaction);

        Set<CongressPosition> congressPositions = congressPositionsManager.getCongressPositions();
        algoSubscriber.updateCongressPositions(congressPositions);
    }
}
