package com.nexus.atp.algos.congress.api;

import com.nexus.atp.common.transaction.TradingSide;
import com.nexus.atp.common.utils.Logger;
import org.json.JSONArray;
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

public class QuiverQuantApiClient {
    private static final SimpleDateFormat queryFormatter = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat responseFormatter = new SimpleDateFormat("yyyy-MM-dd");


    private static final String BASE_URL = "https://api.quiverquant.com/beta/";

    private final String authToken;
    private final HttpClient httpClient;

    private final Logger logger;

    public QuiverQuantApiClient(String authToken, Logger logger) {
        this.authToken = authToken;
        this.httpClient = HttpClient.newHttpClient();
        this.logger = logger;
    }

    private HttpRequest buildRequest(String apiUrl, Map<String, String> params) {
        String paramsString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        String urlString = apiUrl + paramsString;

        return HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .setHeader("Authorization", "Bearer " + authToken)
                .setHeader("Accept", "application/json")
                .build();
    }

    public Map<String, List<HistoricalCongressTrading>> getCongressTrades() {
        String apiUrl = BASE_URL + "bulk/congresstrading?";
        HttpRequest request = buildRequest(apiUrl, Map.of());

        logger.warn("all historical congress trades were requested");
        return getCongressTrades(request);
    }

    public Map<String, List<HistoricalCongressTrading>> getCongressTradesWithCongressId(String congressId) {
        String apiUrl = BASE_URL + "bulk/congresstrading?";
        HttpRequest request = buildRequest(apiUrl, Map.of(
                "bioguide_id", congressId
        ));

        return getCongressTrades(request);
    }

    public Map<String, List<HistoricalCongressTrading>> getCongressTradesWithTicker(String ticker) {
        String apiUrl = BASE_URL + "historical/congresstrading/" + ticker;
        HttpRequest request = buildRequest(apiUrl, Map.of());

        return getCongressTrades(request);
    }

    private boolean responseWasInvalid(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            logger.error("Received non-OK status code (%s): %n%s", response.statusCode(), response.body());
            return true;
        }
        return false;
    }

    private Map<String, List<HistoricalCongressTrading>> getCongressTrades(HttpRequest request) {
        Map<String, List<HistoricalCongressTrading>> congressIdToTrades = new HashMap<>();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (responseWasInvalid(response)) {
                logger.error("response was invalid");
                return null;
            }

            JSONArray body = new JSONArray(response.body());

            for (int i = 0; i < body.length(); i++) {
                JSONObject json = body.getJSONObject(i);

                HistoricalCongressTrading congressTrade;
                try {
                    congressTrade = getCongressTradeFromJSON(json);
                } catch (JSONException e) {
                    logger.error("JSONException on %s: %s", json, e.getMessage());
                    continue;
                }

                String congressId = json.getString("BioGuideID");
                List<HistoricalCongressTrading> congressTrades = congressIdToTrades.computeIfAbsent(congressId, _ -> new ArrayList<>());

                congressTrades.add(congressTrade);
            }

            logger.info("Returning congress trades with %d congressIds", congressIdToTrades.size());
        } catch (IOException e) {
            logger.error("IOException: %s", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException: %s", e.getMessage());
        }

        return congressIdToTrades;
    }

    private HistoricalCongressTrading getCongressTradeFromJSON(JSONObject json) throws JSONException {
        Date transactionDate;
        Date reportingDate;
        try {
            transactionDate = responseFormatter.parse(json.getString("TransactionDate"));
            reportingDate = responseFormatter.parse(json.getString("ReportDate"));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return null;
        }

        TradingSide side = switch (json.getString("Transaction")) {
            case "Purchase" -> TradingSide.BUY;
            case "Sale"     -> TradingSide.SELL;
            default         -> null;
        };

        return new HistoricalCongressTrading(
                json.getString("BioGuideID"),
                json.getString("Ticker"),
                transactionDate,
                reportingDate,
                json.getDouble("Amount"),
                side
        );
    }
}
