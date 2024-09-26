package com.nexus.atp.algos.congress.manager;

import com.nexus.atp.algos.congress.CongressTransaction;
import com.nexus.atp.algos.congress.api.CongressTradesFetcher;
import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.application.ResourcesBuilder;
import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.common.transaction.TradingSide;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Date;

import static com.nexus.atp.utils.Assert.assertEqualsFile;
import static org.junit.jupiter.api.Assertions.*;

public class CongressTradesStorageManagerTest {
    private static final Path transactionsFile = Path.of("src/test/resources/congress-transactions.json");
    private static final Path addedTransactionFile = Path.of("src/test/resources/add-congress-transactions.json");
    private static final Path backupFile = Path.of("src/test/resources/congress-transactions-backup.json");

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private final CongressTradesFetcher congressTradesFetcher = context.mock(CongressTradesFetcher.class);

    private final CongressTradesStorageManager storageManager =
            new CongressTradesStorageManager(transactionsFile.toString(), new ResourcesBuilder().getLogger(), congressTradesFetcher);

    @AfterEach
    public void resetResources() throws IOException {
        Files.copy(backupFile, transactionsFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void storageManagerShouldGetTransactionsFromFile() {
        CongressPosition congressPosition = storageManager.getCongressPosition("M000355");

        StockPosition<CongressTransaction> stockPosition = congressPosition.getStockPosition("LMT");

        CongressTransaction transaction = stockPosition.getTransactions().getFirst();

        assertEquals("LMT", transaction.ticker());
        assertEquals("M000355", transaction.congressId());
        assertEquals(1000, transaction.quantity());
        assertEquals(447.11, transaction.price());
        assertEquals(TradingSide.BUY, transaction.side());
        assertEquals(1725714139156L, transaction.transactionDate().toInstant().toEpochMilli());
        assertEquals(1725783747195L, transaction.reportingDate().toInstant().toEpochMilli());
    }


    @Test
    public void storageManagerShouldPutTransactionsInFile() {
        CongressTransaction transaction = new CongressTransaction(
                "RTX",
                "M000355",
                4000,
                109.38,
                TradingSide.BUY,
                Date.from(Instant.ofEpochMilli(1725714139156L)),
                Date.from(Instant.ofEpochMilli(1725783747195L))
        );

        storageManager.addCongressTransaction(transaction);

        assertEqualsFile(addedTransactionFile, transactionsFile);
    }
}
