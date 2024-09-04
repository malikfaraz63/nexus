package com.nexus.atp.algos.congress.manager;

import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.algos.congress.CongressTransaction;

import java.util.Map;
import java.util.Set;

public interface CongressPositionsManager {
    Map<String, CongressPosition> getAllCongressPositions();
    Set<CongressPosition> getNewCongressPositions();

    CongressPosition getCongressPosition(String congressId);
    void addCongressTransaction(CongressTransaction transaction);
}
