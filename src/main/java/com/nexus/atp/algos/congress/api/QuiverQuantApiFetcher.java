package com.nexus.atp.algos.congress.api;

import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.common.EngineTimeProvider;
import com.nexus.atp.common.scheduled.ScheduledTimer;
import com.nexus.atp.common.utils.Logger;
import com.nexus.atp.marketdata.manager.MarketDataManager;
import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuiverQuantApiFetcher implements CongressTradesFetcher {
    private final QuiverQuantApiClient apiClient;
    private final List<CongressTransactionsSubscriber> congressTransactionsSubscribers;

    private final MarketDataManager marketDataManager;
    private final EngineTimeProvider engineTimeProvider;
    private final ScheduledTimer scheduledTimer;

    public QuiverQuantApiFetcher(String authToken, Logger logger, MarketDataManager marketDataManager, EngineTimeProvider engineTimeProvider, ScheduledTimer scheduledTimer) {
        this.apiClient = new QuiverQuantApiClient(authToken, logger);
        this.congressTransactionsSubscribers = new ArrayList<>();

        this.marketDataManager = marketDataManager;
        this.engineTimeProvider = engineTimeProvider;
        this.scheduledTimer = scheduledTimer;
        
        initializeSubscriptions();
    }

    private void initializeSubscriptions() {
        scheduledTimer.addScheduledCallback(LocalTime.of(8, 0), this::fetchNewCongressTransactions);
    }

    private void fetchNewCongressTransactions() {
        // TODO: validate that API will have today's newly reported congress transactions (i.e. only
        //       after 3 days would it have full today's)
        Map<String, List<HistoricalCongressTrading>> congressIdToTrades = apiClient
            .getCongressTrades(engineTimeProvider.getEngineTime());

        subscribeToNewTickers(congressIdToTrades);

        List<CongressTransaction> transactions = new ArrayList<>();
        for (String congressId : congressIdToTrades.keySet()) {
            List<HistoricalCongressTrading> congressTrades = congressIdToTrades.get(congressId);

            for (HistoricalCongressTrading congressTrade : congressTrades) {
                StockQuoteDaily stockQuote = marketDataManager
                    .getStockQuote(congressTrade.ticker(), congressTrade.transactionDate());

                transactions.add(getCongressTransaction(congressTrade, stockQuote));
            }
        }

        for (CongressTransactionsSubscriber subscriber : congressTransactionsSubscribers) {
            subscriber.onNewTransactions(transactions);
        }
    }

    private void subscribeToNewTickers(Map<String, List<HistoricalCongressTrading>> congressIdToTrades) {
        Set<String> tickers = new HashSet<>();
        for (String congressId : congressIdToTrades.keySet()) {
            List<HistoricalCongressTrading> congressTrades = congressIdToTrades.get(congressId);

            for (HistoricalCongressTrading congressTrade : congressTrades) {
                tickers.add(congressTrade.ticker());
            }
        }
        marketDataManager.subscribeToStocks(tickers);
    }

    @Override
    public void getHistoricCongressTrades(CongressTradesHandler<List<CongressTransaction>> handler) {
        Map<String, List<HistoricalCongressTrading>> congressIdToTrades = apiClient.getCongressTrades();

        subscribeToNewTickers(congressIdToTrades);

        Map<String, List<CongressTransaction>> congressIdToTransactions = new HashMap<>();

        for (String congressId : congressIdToTrades.keySet()) {
            List<HistoricalCongressTrading> congressTrades = congressIdToTrades.get(congressId);

            List<CongressTransaction> transactions = new ArrayList<>();
            for (HistoricalCongressTrading congressTrade : congressTrades) {
                StockQuoteDaily stockQuote = marketDataManager
                    .getStockQuote(congressTrade.ticker(), congressTrade.transactionDate());

                transactions.add(getCongressTransaction(congressTrade, stockQuote));
            }
            congressIdToTransactions.put(congressId, transactions);
        }

        handler.onCongressTrades(congressIdToTransactions);
    }

    @Override
    public void subscribe(CongressTransactionsSubscriber subscriber) {
        congressTransactionsSubscribers.add(subscriber);
        apiClient.getCongressTrades();
    }

    private CongressTransaction getCongressTransaction(HistoricalCongressTrading congressTrade, StockQuoteDaily stockQuote) {
        double price = (stockQuote.open() + stockQuote.close()) / 2.0;
        int quantity = (int) (congressTrade.amount() / price);

        return new CongressTransaction(
            congressTrade.ticker(),
            congressTrade.congressId(),
            quantity,
            price,
            congressTrade.side(),
            congressTrade.transactionDate(),
            congressTrade.reportingDate()
        );
    }
}
