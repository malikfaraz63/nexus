package com.nexus.atp.algos.common;

import com.nexus.atp.algos.congress.CongressPosition;
import java.util.Set;

public interface AlgoSubscriber {
    void updateCongressPositions(Set<CongressPosition> congressPositions);
}
