package com.nexus.atp.marketdata.api;

import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.marketdata.quote.StockQuoteHandler;
import com.nexus.atp.marketdata.quote.StockQuotesHandler;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestMarketDataFetcher implements MarketDataFetcher {
    private final List<String> tickers;
    private final List<Integer> didTriggerCallbacks;
    private final List<StockQuoteHandler> callbackHandlers;
    private final List<LocalTime> scheduledStarts;
    private final List<Long> callbackPeriods;
    private final List<TimeUnit> periodUnits;

    private final List<Set<String>> dailyTickers;
    private final List<StockQuotesHandler<List<StockQuoteDaily>>> dailyCallbackHandlers;
    private final List<LocalTime> dailyScheduledStarts;
    private final List<Boolean> dailyCallbackTriggered;

    private final TestMarketDataProducer producer;

    public TestMarketDataFetcher(TestMarketDataProducer producer) {
        this.tickers = new ArrayList<>();
        this.callbackHandlers = new ArrayList<>();
        this.scheduledStarts = new ArrayList<>();
        this.didTriggerCallbacks = new ArrayList<>();
        this.callbackPeriods = new ArrayList<>();
        this.periodUnits = new ArrayList<>();

        this.dailyTickers = new ArrayList<>();
        this.dailyCallbackHandlers = new ArrayList<>();
        this.dailyScheduledStarts = new ArrayList<>();
        this.dailyCallbackTriggered = new ArrayList<>();

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

                StockQuoteIntraDay quote = producer.getNextIntraDayQuote(tickers.get(i));
                callbackHandlers.get(i).onStockQuote(quote);
            }
        }

        for (int i = 0; i < dailyCallbackHandlers.size(); i++) {
            LocalTime scheduledStart = dailyScheduledStarts.get(i);
            long delta = localTime.toNanoOfDay() - scheduledStart.toNanoOfDay();
            if (delta < 0) {
                continue;
            }

            if (!dailyCallbackTriggered.get(i)) {
                Map<String, List<StockQuoteDaily>> tickerToQuotes = new HashMap<>();
                for (String ticker : dailyTickers.get(i)) {
                    StockQuoteDaily quote = producer.getNextDailyQuote(ticker);
                    tickerToQuotes.put(ticker, List.of(quote));
                }

                dailyCallbackHandlers.get(i).onStockQuotes(tickerToQuotes);
                dailyCallbackTriggered.set(i, true);
            }
        }
    }

    @Override
    public void scheduleStockQuoteDaily(Set<String> tickers, LocalTime scheduledStart, StockQuotesHandler<List<StockQuoteDaily>> handler) {
        dailyTickers.add(tickers);
        dailyCallbackHandlers.add(handler);
        dailyScheduledStarts.add(scheduledStart);
        dailyCallbackTriggered.add(false);
    }

    @Override
    public void getHistoricStockQuotes(Set<String> tickers, StockQuotesHandler<List<StockQuoteDaily>> handler) {
        Map<String, List<StockQuoteDaily>> tickerToQuotes = new HashMap<>();
        for (String ticker : tickers) {
            StockQuoteDaily quote = producer.getNextDailyQuote(ticker);
            tickerToQuotes.put(ticker, List.of(quote));
        }

        handler.onStockQuotes(tickerToQuotes);
    }

    @Override
    public void subscribeToStockIntraDay(String ticker, LocalTime scheduledStart, long callbackPeriod, TimeUnit periodUnit, StockQuoteHandler handler) {
        tickers.add(ticker);
        callbackHandlers.add(handler);
        scheduledStarts.add(scheduledStart);
        didTriggerCallbacks.add(0);
        callbackPeriods.add(callbackPeriod);
        periodUnits.add(periodUnit);
    }

    @Override
    public void getStockQuotes(Set<String> tickers, StockQuotesHandler<StockQuoteIntraDay> handler) {
        Map<String, StockQuoteIntraDay> stockTickerToQuote = new HashMap<>();

        for (String ticker : tickers) {
            StockQuoteIntraDay quote = producer.getNextIntraDayQuote(ticker);
            stockTickerToQuote.put(ticker, quote);
        }

        handler.onStockQuotes(stockTickerToQuote);
    }
}