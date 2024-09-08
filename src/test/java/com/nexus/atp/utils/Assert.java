package com.nexus.atp.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assert {
    public static void assertEqualsFile(Path expectedFile, Path actualFile) {
        try {
            String expectedContents = Files.readString(expectedFile);
            String actualContents = Files.readString(actualFile);

            assertEquals(expectedContents, actualContents);
        } catch (IOException e) {
            assert false;
        }
    }
}
