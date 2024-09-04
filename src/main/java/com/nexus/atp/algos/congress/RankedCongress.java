package com.nexus.atp.algos.congress;

public class RankedCongress implements Comparable<RankedCongress> {
    private final String congressId;
    private final double profitability;

    public RankedCongress(String congressId, double profitability) {
        this.congressId = congressId;
        this.profitability = profitability;
    }

    public String getCongressId() {
        return congressId;
    }

    @Override
    public int compareTo(RankedCongress other) {
        return Double.compare(profitability, other.profitability);
    }
}
