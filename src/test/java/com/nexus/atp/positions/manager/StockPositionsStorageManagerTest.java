package com.nexus.atp.positions.manager;

import com.nexus.atp.common.stock.StockPosition;
import com.nexus.atp.common.transaction.TradingSide;
import com.nexus.atp.positions.PositionTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class StockPositionsStorageManagerTest {
    private final Path transactionsFile = Path.of("src/test/resources/position-transactions.json");
    private final Path addedTransactionFile = Path.of("src/test/resources/add-position-transactions.json");
    private final Path backupFile = Path.of("src/test/resources/position-transactions-backup.json");

    private final StockPositionsStorageManager stockPositionsStorageManager =
            new StockPositionsStorageManager(transactionsFile.toString());

    @AfterEach
    public void resetResources() throws IOException {
        Files.copy(backupFile, transactionsFile, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void storageManagerShouldGetTransactionsFromFile() {
        StockPosition<PositionTransaction> position = stockPositionsStorageManager.getStockPosition("AAPL");

        PositionTransaction transaction = position.getTransactions().getFirst();

        assertEquals("AAPL", transaction.ticker());
        assertEquals(50, transaction.quantity());
        assertEquals(210.41, transaction.price());
        assertEquals(TradingSide.BUY, transaction.side());
        assertEquals(1725714139156L, transaction.transactionDate().toInstant().toEpochMilli());
    }

    @Test
    public void storageManagerShouldPutTransactionsInFile() {
        PositionTransaction transaction = new PositionTransaction(
                "GOOGL",
                10,
                450.13,
                TradingSide.BUY,
                Date.from(Instant.ofEpochMilli(1725714139156L))
        );

        stockPositionsStorageManager.addPositionTransaction(transaction);

        assertEqualsFile(addedTransactionFile, transactionsFile);
    }

    private static void assertEqualsFile(Path expectedFile, Path actualFile) {
        try {
            String expectedContents = Files.readString(expectedFile);
            String actualContents = Files.readString(actualFile);

            assertEquals(expectedContents, actualContents);
        } catch (IOException e) {
            assert false;
        }
    }
}
