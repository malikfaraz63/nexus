package com.nexus.atp.positions.controls;

public class StockPositionControlsConfig {
    private final double takeProfitMargin;
    private final double stopLossMargin;

    public StockPositionControlsConfig(double takeProfitMargin, double stopLossMargin) {
        if (takeProfitMargin <= 0 || takeProfitMargin > 1) {
            throw new IllegalArgumentException("takeProfitMargin must be between 0 and 1");
        }
        if (stopLossMargin <= 0 || stopLossMargin > 1) {
            throw new IllegalArgumentException("stopLossMargin must be between 0 and 1");
        }

        this.takeProfitMargin = takeProfitMargin;
        this.stopLossMargin = stopLossMargin;
    }

    public double getTakeProfitMargin() {
        return takeProfitMargin;
    }

    public double getStopLossMargin() {
        return stopLossMargin;
    }
}
