package com.nexus.atp.marketdata.manager;

import com.nexus.atp.common.storage.BaseStorageManager;
import com.nexus.atp.marketdata.MarketDataConfig;
import com.nexus.atp.marketdata.api.MarketDataFetcher;
import com.nexus.atp.marketdata.quote.StockQuote;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.*;

public class MarketDataStorageManager extends BaseStorageManager implements MarketDataManager {
    private final MarketDataFetcher marketDataFetcher;
    private final MarketDataConfig marketDataConfig;

    private final Map<String, List<StockQuote>> stockTickerToQuote;

    public MarketDataStorageManager(MarketDataFetcher marketDataFetcher, MarketDataConfig marketDataConfig, String filePath) {
        super(filePath);
        this.marketDataFetcher = marketDataFetcher;
        this.marketDataConfig = marketDataConfig;
        this.stockTickerToQuote = new HashMap<>();

        initializeMarketData();
    }

    public void initializeMarketData() {
        JSONArray stockTickersJson = fileContents.getJSONArray("stockTickers");

        List<String> stockTickers = new ArrayList<>();
        for (int i = 0; i < stockTickersJson.length(); i++) {
            stockTickers.add(stockTickersJson.getString(i));
        }

        JSONObject marketDataJson = fileContents.getJSONObject("marketData");
        for (String ticker : stockTickers) {
            JSONArray stockDataJson = marketDataJson.getJSONArray(ticker);

            List<StockQuote> stockQuotes = new ArrayList<>();
            for (int i = 0; i < stockDataJson.length(); i++) {
                JSONObject stockJson = stockDataJson.getJSONObject(i);
                stockQuotes.add(getStockQuoteFromJSON(ticker, stockJson));
            }

            stockTickerToQuote.put(ticker, stockQuotes);
        }

        stockTickers.forEach(this::subscribeToStockTicker);
    }

    private void subscribeToStockTicker(String ticker) {
        marketDataFetcher.subscribeToStock(
                ticker,
                marketDataConfig.getMarketDataStartTime(),
                marketDataConfig.getMarketDataCallbackPeriod(), marketDataConfig.getMarketDataPeriodUnit(),
                this::onStockQuote);
    }

    private StockQuote getStockQuoteFromJSON(String ticker, JSONObject json) {
        return new StockQuote(
                ticker,
                json.getDouble("price"),
                Date.from(Instant.ofEpochMilli(json.getLong("timestamp")))
        );
    }

    @Override
    public double getMaxPriceForStock(String ticker, Date fromDate, Date toDate) {
        assert fromDate.before(toDate);

        List<StockQuote> stockQuotes = stockTickerToQuote.get(ticker);
        if (stockQuotes == null) {
            throw new IllegalArgumentException("Ticker with no data: " + ticker);
        }

        double maxPrice = Double.MIN_VALUE;

        int i = 0;
        while (stockQuotes.get(i).getTimestamp().before(fromDate)) {
            i++;
        }

        while (i < stockQuotes.size() && stockQuotes.get(i).getTimestamp().before(toDate)) {
            maxPrice = Math.max(maxPrice, stockQuotes.get(i).getPrice());
            i++;
        }

        return maxPrice;
    }

    private void onStockQuote(StockQuote stockQuote) {
        stockTickerToQuote.get(stockQuote.getTicker()).add(stockQuote);

        JSONObject json = getJSONFromStockQuote(stockQuote);
        JSONArray stockDataJson = fileContents
                .getJSONObject("marketData")
                .getJSONArray(stockQuote.getTicker());
        stockDataJson.put(json);

        super.writeFileContents();
    }

    private JSONObject getJSONFromStockQuote(StockQuote stockQuote) {
        JSONObject json = new JSONObject();

        json.put("price", stockQuote.getPrice());
        json.put("timestamp", stockQuote.getTimestamp());

        return json;
    }
}
