package com.nexus.atp.marketdata.manager;

import com.nexus.atp.common.storage.BaseStorageManager;
import com.nexus.atp.marketdata.MarketDataConfig;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MarketDataStorageManager extends BaseStorageManager implements MarketDataManager {
    private final MarketDataFetcher marketDataFetcher;
    private final MarketDataConfig marketDataConfig;

    private final Map<String, List<StockQuoteDaily>> stockTickerToQuotes;

    public MarketDataStorageManager(MarketDataFetcher marketDataFetcher, MarketDataConfig marketDataConfig, String filePath) {
        super(filePath);
        this.marketDataFetcher = marketDataFetcher;
        this.marketDataConfig = marketDataConfig;
        this.stockTickerToQuotes = new HashMap<>();

        initializeMarketData();
    }

    private void initializeMarketData() {
        JSONArray stockTickersJson = fileContents.getJSONArray("stockTickers");

        List<String> stockTickers = new ArrayList<>();
        for (int i = 0; i < stockTickersJson.length(); i++) {
            stockTickers.add(stockTickersJson.getString(i));
        }

        JSONObject marketDataJson = fileContents.getJSONObject("marketData");
        for (String ticker : stockTickers) {
            JSONArray stockDataJson = marketDataJson.getJSONArray(ticker);

            List<StockQuoteDaily> stockQuotes = new ArrayList<>();
            for (int i = 0; i < stockDataJson.length(); i++) {
                JSONObject stockJson = stockDataJson.getJSONObject(i);
                stockQuotes.add(getStockQuoteFromJSON(ticker, stockJson));
            }

            stockTickerToQuotes.put(ticker, stockQuotes);
        }

        marketDataFetcher.scheduleStockQuoteDaily(
                stockTickerToQuotes.keySet(),
                marketDataConfig.getEndOfDayStockQuotesTime(),
                this::onStockQuotesDaily
        );
    }

    private void onStockQuotesDaily(Map<String, List<StockQuoteDaily>> stockQuoteDaily) {
        for (String ticker : stockQuoteDaily.keySet()) {
            if (!stockTickerToQuotes.containsKey(ticker)) {
                throw new IllegalArgumentException("Data for unsubscribed ticker: " + ticker);
            }

            List<StockQuoteDaily> currentStockQuotes = stockTickerToQuotes.get(ticker);
            List<StockQuoteDaily> stockQuotesUpdate = stockQuoteDaily.get(ticker);

            int i = 0;
            if (!currentStockQuotes.isEmpty()) {
                while (i < (stockQuotesUpdate.size() - 1) && stockQuotesUpdate.get(i).getTimestamp().before(currentStockQuotes.getLast().getTimestamp())) {
                    i++;
                }

                if (i == (stockQuotesUpdate.size() + 1)) {
                    continue;
                }
            }

            List<StockQuoteDaily> stockQuotesNew = stockQuotesUpdate.subList(i, stockQuotesUpdate.size());

            currentStockQuotes.addAll(stockQuotesNew);
            // need to write to JSON

            for (StockQuoteDaily stockQuote : stockQuotesNew) {
                JSONObject json = getJSONFromStockQuote(stockQuote);
                JSONArray stockDataJson = fileContents
                        .getJSONObject("marketData")
                        .getJSONArray(ticker);

                stockDataJson.put(json);
            }
        }

        super.writeFileContents();
    }

    @Override
    public void subscribeToStocks(Set<String> tickers) {
        Set<String> newTickers = tickers
                .stream()
                .filter(ticker -> !stockTickerToQuotes.containsKey(ticker))
                .collect(Collectors.toSet());

        if (newTickers.isEmpty()) {
            return;
        }

        for (String ticker : newTickers) {
            stockTickerToQuotes.put(ticker, new ArrayList<>());
            fileContents
                    .getJSONArray("stockTickers")
                    .put(ticker);
            fileContents
                    .getJSONObject("marketData")
                    .put(ticker, List.of());
        }

        marketDataFetcher.getHistoricStockQuotes(newTickers, this::onStockQuotesDaily);

        super.writeFileContents();
    }

    @Override
    public List<StockQuoteDaily> getStockQuotes(String ticker) {
        return stockTickerToQuotes.get(ticker);
    }

    private List<StockQuoteDaily> fetchStockQuotes(String ticker) {
        List<StockQuoteDaily> stockQuotes = stockTickerToQuotes.get(ticker);
        if (stockQuotes == null) {
            throw new IllegalArgumentException("No stock quotes found for ticker: " + ticker);
        }

        return stockQuotes;
    }

    @Override
    public double getMaxPriceForStock(String ticker, Date fromDate, Date toDate) {
        assert fromDate.before(toDate);

        List<StockQuoteDaily> stockQuotes = fetchStockQuotes(ticker);

        double maxPrice = Double.MIN_VALUE;

        int i = 0;
        while (stockQuotes.get(i).getTimestamp().before(fromDate)) {
            i++;
        }

        while (i < stockQuotes.size() && stockQuotes.get(i).getTimestamp().before(toDate)) {
            maxPrice = Math.max(maxPrice, stockQuotes.get(i).getHigh());
            i++;
        }

        return maxPrice;
    }

    @Override
    public StockQuoteDaily getStockQuote(String ticker, Date transactionDate) {
        List<StockQuoteDaily> stockQuotes = fetchStockQuotes(ticker);

        int i = 0;

        long searchMillis = transactionDate.toInstant().toEpochMilli();
        long diff = Long.MAX_VALUE;
        while (i < stockQuotes.size()) {
            long currentMillis = stockQuotes.get(i).getTimestamp().toInstant().toEpochMilli();
            long currentDiff = Math.abs(currentMillis - searchMillis);
            if (currentDiff < diff) {
                diff = currentDiff;
            } else {
                return stockQuotes.get(i);
            }
            i++;
        }

        return null;
    }

    private StockQuoteDaily getStockQuoteFromJSON(String ticker, JSONObject json) {
        return new StockQuoteDaily(
                ticker,
                json.getDouble("open"),
                json.getDouble("high"),
                json.getDouble("low"),
                json.getDouble("close"),
                json.getDouble("volume"),
                Date.from(Instant.ofEpochMilli(json.getLong("timestamp")))
        );
    }

    private JSONObject getJSONFromStockQuote(StockQuoteDaily stockQuote) {
        JSONObject json = new JSONObject();

        json.put("open", stockQuote.getOpen());
        json.put("high", stockQuote.getHigh());
        json.put("low", stockQuote.getLow());
        json.put("close", stockQuote.getClose());
        json.put("volume", stockQuote.getVolume());
        json.put("timestamp", stockQuote.getTimestamp().toInstant().toEpochMilli());

        return json;
    }
}
