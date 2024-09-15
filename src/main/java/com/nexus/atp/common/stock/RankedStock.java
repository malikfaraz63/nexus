package com.nexus.atp.common.stock;

public class RankedStock implements Comparable<RankedStock> {
    private final String ticker;
    private double tradedVolume;
    private double buyVolume;

    public RankedStock(String ticker) {
        this.ticker = ticker;
        this.tradedVolume = 0;
    }

    public String getTicker() {
        return ticker;
    }

    public void addTradedVolume(double tradedVolume) {
        assert tradedVolume > 0;

        this.tradedVolume += tradedVolume;
    }

    @Override
    public int compareTo(RankedStock other) {
        return Double.compare(getRank(), other.getRank());
    }

    public void addBuyVolume(double buyVolume) {
        assert buyVolume >= 0;

        this.buyVolume = buyVolume;
    }

    private double getRank() {
        double buyRatio = 2 * (buyVolume / tradedVolume) - 1;
        return buyRatio * tradedVolume;
    }
}