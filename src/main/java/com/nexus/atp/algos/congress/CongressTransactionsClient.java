package com.nexus.atp.algos.congress;

import com.nexus.atp.algos.congress.api.CongressTradesFetcher;
import com.nexus.atp.algos.congress.api.CongressTransactionsSubscriber;
import com.nexus.atp.algos.congress.engine.CongressTradesEngineConfig;
import com.nexus.atp.algos.congress.manager.CongressPositionsManager;
import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.algos.congress.position.CongressPositionsSubscriber;
import com.nexus.atp.common.scheduled.ScheduledTimer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CongressTransactionsClient implements CongressTransactionsSubscriber {
    private final CongressPositionsSubscriber congressPositionsSubscriber;
    private final CongressPositionsManager congressPositionsManager;

    public CongressTransactionsClient(CongressTradesEngineConfig config,
                                      CongressPositionsSubscriber congressPositionsSubscriber,
                                      CongressPositionsManager congressPositionsManager,
                                      CongressTradesFetcher congressTradesFetcher,
                                      ScheduledTimer timer) {
        this.congressPositionsSubscriber = congressPositionsSubscriber;
        this.congressPositionsManager = congressPositionsManager;

        congressTradesFetcher.subscribe(this);
        timer.addScheduledCallback(config.getCongressTradesLoadTime(), this::loadCongressPositions);
    }

    private void loadCongressPositions() {
        Map<String, CongressPosition> congressPositions = congressPositionsManager.getAllCongressPositions();
        congressPositionsSubscriber.initializeCongressPositions(congressPositions);
    }

    @Override
    public void onNewTransactions(List<CongressTransaction> transactions) {
        transactions.forEach(congressPositionsManager::addCongressTransaction);

        Set<CongressPosition> newCongressPositions = congressPositionsManager.getNewCongressPositions();
        congressPositionsSubscriber.updateNewCongressPositions(newCongressPositions);
    }
}
