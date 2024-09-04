package com.nexus.atp.positions.hold;

import java.util.List;
import java.util.Set;

public interface HoldDecision {
    List<StocksHold> getStocksHolds();
    Set<String> getTickers();
}
