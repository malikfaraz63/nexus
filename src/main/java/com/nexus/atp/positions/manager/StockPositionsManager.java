package com.nexus.atp.positions.manager;

import com.nexus.atp.positions.PositionTransaction;
import com.nexus.atp.common.StockPosition;
import java.util.Set;

/**
 *
 */
public interface StockPositionsManager {
    Set<StockPosition<PositionTransaction>> getStockPositions();
    void addPositionTransaction(PositionTransaction transaction);
}
