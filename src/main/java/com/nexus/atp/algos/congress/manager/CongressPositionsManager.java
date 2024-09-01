package com.nexus.atp.algos.congress.manager;

import com.nexus.atp.algos.congress.CongressPosition;
import com.nexus.atp.algos.congress.CongressTransaction;
import java.util.Set;

public interface CongressPositionsManager {
    Set<CongressPosition> getCongressPositions();
    CongressPosition getCongressPosition(String congressId);
    void addCongressTransaction(CongressTransaction transaction);
}
