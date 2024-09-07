package com.nexus.atp.marketdata.manager;

import java.util.Date;

public interface MarketDataManager {
    double getMaxPriceForStock(String ticker, Date fromDate, Date toDate);
}
