package com.nexus.atp.marketdata.api;

import com.nexus.atp.common.utils.Logger;
import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AlphaVantageApiClient {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static final String BASE_URL = "https://www.alphavantage.co/query?";

    private final String apiKey;
    private final HttpClient httpClient;

    private final Logger logger;

    public AlphaVantageApiClient(String apiKey, Logger logger) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.logger = logger;
    }

    private String buildUrlString(Map<String, String> params) {
        String paramsString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        return BASE_URL + paramsString + "&apikey=" + apiKey;
    }

    private boolean responseWasInvalid(HttpResponse<String> response) {
        if (response.body().contains("rate limit")) {
            return true;
        }
        if (response.body().contains("Error Message")) {
            JSONObject errorMessage = new JSONObject(response.body());
            logger.error(errorMessage.getString("Error Message"));
            return true;
        }

        return false;
    }

    public List<StockQuoteDaily> getHistoricStockQuotes(String ticker, boolean isFullHistory) {
        Map<String, String> params = Map.of(
                "symbol", ticker,
                "function", "TIME_SERIES_DAILY"
        );
        if (isFullHistory) {
            params = new HashMap<>(params);
            params.put("outputsize", "full");
        }

        String urlString = buildUrlString(params);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();

        List<StockQuoteDaily> stockQuotes = new ArrayList<>();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (responseWasInvalid(response)) {
                logger.error("Response not valid");
                return stockQuotes;
            }

            JSONObject body = new JSONObject(response.body());
            JSONObject timeSeries = body.getJSONObject("Time Series (Daily)");

            Iterator<String> keys = timeSeries.keys();
            while (keys.hasNext()) {
                String date = keys.next();
                JSONObject quote = timeSeries.getJSONObject(date);

                Date timestamp;
                StockQuoteDaily stockQuote;
                try {
                    timestamp = getTimestamp(date);
                    stockQuote = getStockQuoteDailyFromJSON(ticker, timestamp, quote);
                } catch (ParseException e) {
                    logger.error("Timestamp ParseException on %s: %s", quote, e.getMessage());
                    continue;
                }  catch (JSONException e) {
                    logger.error("JSONException on %s: %s", quote, e.getMessage());
                    continue;
                }

                stockQuotes.add(stockQuote);
            }
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException: " + e.getMessage());
        }

        return stockQuotes;
    }

    public StockQuoteIntraDay getStockQuoteIntraDay(String ticker) {
        String urlString = buildUrlString(Map.of(
                "symbol", ticker,
                "function", "GLOBAL_QUOTE"
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();

        StockQuoteIntraDay stockQuote = null;

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (responseWasInvalid(response)) {
                logger.error("Response not valid");
                return null;
            }

            JSONObject body = new JSONObject(response.body());

            stockQuote = getStockQuoteIntraDayFromJSON(ticker, body);
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException: " + e.getMessage());
        } catch (JSONException e) {
            logger.error("JSONException: " + e.getMessage());
        }

        return stockQuote;
    }

    private StockQuoteIntraDay getStockQuoteIntraDayFromJSON(String ticker, JSONObject json) {
        return new StockQuoteIntraDay(
                ticker,
                json.getDouble("05. price"),
                new Date()
        );
    }

    private StockQuoteDaily getStockQuoteDailyFromJSON(String ticker, Date timestamp, JSONObject json) throws JSONException {
        return new StockQuoteDaily(
                ticker,
                json.getDouble("1. open"),
                json.getDouble("2. high"),
                json.getDouble("3. low"),
                json.getDouble("4. close"),
                json.getDouble("5. volume"),
                timestamp
        );
    }

    private static Date getTimestamp(String date) throws ParseException {
        return formatter.parse(date);
    }
}
