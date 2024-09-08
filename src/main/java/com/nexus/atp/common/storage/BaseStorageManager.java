package com.nexus.atp.common.storage;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaseStorageManager {
    private final String filePath;
    private final Path path;

    protected JSONObject fileContents;

    public BaseStorageManager(String filePath) {
        this.filePath = filePath;
        this.path = Paths.get(filePath);

        initialize();
    }

    private void initialize() {
        try {
            String fileContents = new String(Files.readAllBytes(path));
            this.fileContents = new JSONObject(fileContents);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void writeFileContents() {
        try {
            Files.write(Paths.get(filePath), fileContents.toString(2).getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
