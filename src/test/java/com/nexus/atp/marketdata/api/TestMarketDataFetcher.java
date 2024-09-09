package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuote;
import com.nexus.atp.marketdata.quote.StockQuoteHandler;
import com.nexus.atp.marketdata.quote.StockQuotesHandler;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TestMarketDataFetcher implements MarketDataFetcher {
    private final List<String> tickers;
    private final List<Integer> didTriggerCallbacks;
    private final List<StockQuoteHandler> callbackHandlers;
    private final List<LocalTime> scheduledStarts;
    private final List<Long> callbackPeriods;
    private final List<TimeUnit> periodUnits;

    private final TestMarketDataProducer producer;

    public TestMarketDataFetcher(TestMarketDataProducer producer) {
        this.tickers = new ArrayList<>();
        this.callbackHandlers = new ArrayList<>();
        this.scheduledStarts = new ArrayList<>();
        this.didTriggerCallbacks = new ArrayList<>();
        this.callbackPeriods = new ArrayList<>();
        this.periodUnits = new ArrayList<>();

        this.producer = producer;
    }

    public void advanceTimeTo(LocalTime localTime) {
        for (int i = 0; i < callbackHandlers.size(); i++) {
            LocalTime scheduledStart = scheduledStarts.get(i);
            long delta = localTime.toNanoOfDay() - scheduledStart.toNanoOfDay();
            if (delta < 0) {
                continue;
            }

            long index = delta / (periodUnits.get(i).toNanos(callbackPeriods.get(i)));

            while (index >= didTriggerCallbacks.get(i)) {
                didTriggerCallbacks.set(i, didTriggerCallbacks.get(i) + 1);

                StockQuote quote = producer.getNextQuote(tickers.get(i));
                callbackHandlers.get(i).onStockQuote(quote);
            }
        }
    }

    @Override
    public void subscribeToStock(String ticker, LocalTime scheduledStart, long callbackPeriod, TimeUnit periodUnit, StockQuoteHandler handler) {
        tickers.add(ticker);
        callbackHandlers.add(handler);
        scheduledStarts.add(scheduledStart);
        didTriggerCallbacks.add(0);
        callbackPeriods.add(callbackPeriod);
        periodUnits.add(periodUnit);
    }

    @Override
    public void getStockQuotes(Set<String> tickers, StockQuotesHandler handler) {

    }
}