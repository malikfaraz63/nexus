package com.nexus.atp.algos.congress.position;

import java.util.Map;
import java.util.Set;

public interface CongressPositionsSubscriber {
    void initializeCongressPositions(Map<String, CongressPosition> congressIdToPosition);
    void updateNewCongressPositions(Set<CongressPosition> congressIdToPosition);
}
