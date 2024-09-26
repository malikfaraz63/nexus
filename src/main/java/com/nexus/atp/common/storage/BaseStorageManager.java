package com.nexus.atp.common.storage;

import com.nexus.atp.common.utils.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseStorageManager {
    private final String filePath;
    private final Path path;

    protected JSONObject fileContents;

    private final Logger logger;

    public BaseStorageManager(String filePath, Logger logger) {
        this.filePath = filePath;
        this.path = Paths.get(filePath);
        this.logger = logger;

        initialize();
    }

    private void initialize() {
        try {
            String fileContents = new String(Files.readAllBytes(path));
            this.fileContents = new JSONObject(fileContents);
        } catch (IOException e) {
            logger.error("IOException when loading contents from file:\n" + e.getMessage());
        }
    }

    protected void writeFileContents() {
        try {
            Files.write(Paths.get(filePath), fileContents.toString(2).getBytes());
        } catch (IOException e) {
            logger.error("IOException when writing contents to file:\n" + e.getMessage());
        }
    }
}
