package com.nexus.atp.marketdata.manager;

import com.nexus.atp.marketdata.MarketDataConfig;
import com.nexus.atp.marketdata.api.TestMarketDataFetcher;
import com.nexus.atp.marketdata.api.TestMarketDataProducer;
import com.nexus.atp.marketdata.quote.StockQuoteDaily;
import com.nexus.atp.marketdata.quote.StockQuoteIntraDay;
import com.nexus.atp.positions.engine.StockPositionsEngineConfig;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static com.nexus.atp.utils.Assert.assertEqualsFile;

class MarketDataStorageManagerTest {
    private static final Path newSubscriptionFile = Paths.get("src/test/resources/market-data-new-sub.json");
    private static final Path newDataFile = Paths.get("src/test/resources/market-data-new-data.json");
    private static final Path marketDataFile = Paths.get("src/test/resources/market-data.json");
    private static final Path backupFile = Paths.get("src/test/resources/market-data-backup.json");

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private final TestMarketDataFetcher marketDataFetcher = new TestMarketDataFetcher(new TestMarketDataProducer() {
        @Override
        public StockQuoteIntraDay getNextIntraDayQuote(String ticker) {
            return marketDataProducerForIntraDay(ticker);
        }

        @Override
        public StockQuoteDaily getNextDailyQuote(String ticker) {
            return marketDataProducerForDaily(ticker);
        }
    });
    private final MarketDataConfig config = new StockPositionsEngineConfig(
            LocalTime.of(19, 0),
            LocalTime.of(6, 0),
            30
    );

    @AfterEach
    public void resetResources() throws IOException {
        Files.copy(backupFile, marketDataFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void storageManagerShouldGetMarketDataFromFile() {
        MarketDataStorageManager marketDataStorageManager = new MarketDataStorageManager(marketDataFetcher, config, marketDataFile.toString());

        List<StockQuoteDaily> stockMarketData = marketDataStorageManager.getStockQuotes("AAPL");
        assertEquals(1, stockMarketData.size());
    }

    @Test
    public void storageManagerShouldPutNewMarketDataInFile() {
        MarketDataStorageManager marketDataStorageManager = new MarketDataStorageManager(marketDataFetcher, config, marketDataFile.toString());

        marketDataFetcher.advanceTimeTo(LocalTime.of(19, 0));

        List<StockQuoteDaily> stockMarketData = marketDataStorageManager.getStockQuotes("AAPL");
        assertEquals(2, stockMarketData.size());

        assertEqualsFile(marketDataFile, newDataFile);
    }

    @Test
    public void storageManagerShouldAllowNewStockSubscription() {
        MarketDataStorageManager marketDataStorageManager = new MarketDataStorageManager(marketDataFetcher, config, marketDataFile.toString());


        List<StockQuoteDaily> stockMarketData1 = marketDataStorageManager.getStockQuotes("AAPL");
        assertEquals(1, stockMarketData1.size());

        List<StockQuoteDaily> stockMarketData2 = marketDataStorageManager.getStockQuotes("GOOGL");
        assertNull(stockMarketData2);

        marketDataStorageManager.subscribeToStocks(Set.of("GOOGL"));

        stockMarketData2 = marketDataStorageManager.getStockQuotes("GOOGL");
        assertEquals(1, stockMarketData2.size());

        marketDataFetcher.advanceTimeTo(LocalTime.of(19, 0));

        stockMarketData1 = marketDataStorageManager.getStockQuotes("AAPL");
        assertEquals(2, stockMarketData1.size());

        stockMarketData2 = marketDataStorageManager.getStockQuotes("GOOGL");
        assertEquals(2, stockMarketData2.size());

        assertEqualsFile(marketDataFile, newSubscriptionFile);
    }

    private StockQuoteIntraDay marketDataProducerForIntraDay(String ticker) {
        return switch (ticker) {
            case "AAPL"  -> new StockQuoteIntraDay(ticker, 100.15, Date.from(Instant.ofEpochMilli(1725728139156L)));
            case "GOOGL" -> new StockQuoteIntraDay(ticker, 104.85, Date.from(Instant.ofEpochMilli(1725728139156L)));
            default      -> new StockQuoteIntraDay(ticker, 200.57, Date.from(Instant.MIN));
        };
    }

    private StockQuoteDaily marketDataProducerForDaily(String ticker) {
        return switch (ticker) {
            case "AAPL" ->
                    new StockQuoteDaily(ticker, 145.30, 147.50, 143.10, 146.00, 5000000, Date.from(Instant.ofEpochMilli(1725728139156L)));
            case "GOOGL" ->
                    new StockQuoteDaily(ticker, 2520.00, 2540.20, 2500.00, 2535.50, 3000000, Date.from(Instant.ofEpochMilli(1725728139156L)));
            default ->
                    new StockQuoteDaily(ticker, 200.00, 205.00, 195.00, 202.50, 8000000, Date.from(Instant.MIN));
        };
    }
}