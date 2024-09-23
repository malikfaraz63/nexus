package com.nexus.atp.marketdata.api;

import com.nexus.atp.common.scheduled.ScheduledTimer;
import com.nexus.atp.common.utils.Logger;
import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteHandler;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.marketdata.quote.StockQuotesHandler;

import java.time.LocalTime;
import java.util.*;

import java.util.concurrent.*;

public class AlphaVantageApiFetcher implements MarketDataFetcher {
    private final AlphaVantageApiClient apiClient;
    private final ScheduledTimer scheduledTimer;
    private final Logger logger;

    private final ExecutorService executorService;

    public AlphaVantageApiFetcher(String apiKey, ScheduledTimer scheduledTimer, Logger logger) {
        this.apiClient = new AlphaVantageApiClient(apiKey, logger);
        this.scheduledTimer = scheduledTimer;
        this.logger = logger;
        this.executorService = Executors.newFixedThreadPool(128);
    }

    @Override
    public void scheduleStockQuoteDaily(Set<String> tickers,
                                        LocalTime scheduledStart,
                                        StockQuotesHandler<List<StockQuoteDaily>> handler) {
        scheduledTimer.addScheduledCallback(scheduledStart, () -> onFetchStockQuotesDaily(tickers, handler, false));
    }

    @Override
    public void getHistoricStockQuotes(Set<String> tickers, StockQuotesHandler<List<StockQuoteDaily>> handler) {
        onFetchStockQuotesDaily(tickers, handler, true);
    }

    private void onFetchStockQuotesDaily(Set<String> tickers, StockQuotesHandler<List<StockQuoteDaily>> handler, boolean isFullHistory) {
        Map<String, List<StockQuoteDaily>> stockTickerToQuotes = new ConcurrentHashMap<>();

        List<Callable<List<StockQuoteDaily>>> stockQuotesTasks = new ArrayList<>();
        for (String ticker : tickers) {
            stockQuotesTasks.add(() -> apiClient.getHistoricStockQuotes(ticker, isFullHistory));
        }

        try {
            List<Future<List<StockQuoteDaily>>> stockQuotesResults = executorService.invokeAll(stockQuotesTasks, 5, TimeUnit.SECONDS);

            for (Future<List<StockQuoteDaily>> stockQuotesResult : stockQuotesResults) {
                try {
                    List<StockQuoteDaily> stockQuotes = stockQuotesResult.get();
                    if (stockQuotes.isEmpty()) continue;

                    String ticker = stockQuotes.getFirst().getTicker();
                    stockTickerToQuotes.put(ticker, stockQuotes);
                } catch (CancellationException e) {
                    logger.error("Cancellation due to timeout while fetching stock quote: %s", e.getMessage());
                }
            }

            logger.info("retrieved %d stock quotes", stockQuotesResults.size());
            handler.onStockQuotes(stockTickerToQuotes);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            logger.error("Interrupted while fetching stock quotes: %s", e.getMessage());
        } catch (ExecutionException e) {
            logger.error("Execution exception while fetching stock quotes: %s", e.getMessage());
        }
    }

    @Override
    public synchronized void subscribeToStockIntraDay(String ticker,
                                                      LocalTime scheduledStart, long callbackPeriod, TimeUnit periodUnit,
                                                      StockQuoteHandler handler) {
        scheduledTimer.addScheduledSubscription(
                scheduledStart,
                callbackPeriod,
                periodUnit,
                () -> {
                    StockQuoteIntraDay stockQuote = apiClient.getStockQuoteIntraDay(ticker);
                    handler.onStockQuote(stockQuote);
                }
        );
    }

    @Override
    public synchronized void getStockQuotes(Set<String> tickers,
                                            StockQuotesHandler<StockQuoteIntraDay> handler) {
        Map<String, StockQuoteIntraDay> stockTickerToQuote = new ConcurrentHashMap<>();

        List<Callable<StockQuoteIntraDay>> stockQuoteTasks = new ArrayList<>();
        for (String ticker : tickers) {
            stockQuoteTasks.add(() -> apiClient.getStockQuoteIntraDay(ticker));
        }

        try {
            List<Future<StockQuoteIntraDay>> stockQuoteResults = executorService.invokeAll(stockQuoteTasks);

            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                return; // TODO: exception callback
            }

            for (Future<StockQuoteIntraDay> stockQuoteResult : stockQuoteResults) {
                StockQuoteIntraDay stockQuote = stockQuoteResult.get();
                stockTickerToQuote.put(stockQuote.ticker(), stockQuote);
            }
            handler.onStockQuotes(stockTickerToQuote);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            executorService.shutdownNow();
        } catch (ExecutionException e) {
            System.out.println(e.getMessage());
        }
    }
}
