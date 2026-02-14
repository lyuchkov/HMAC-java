package ru.yandex.practicum.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private final Gson gson = new Gson();

    public AppConfig load(String fileName) throws IOException {
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            throw new IOException("Configuration file not found: " + fileName);
        }

        try {
            String jsonContent = Files.readString(path);
            AppConfig config = gson.fromJson(jsonContent, AppConfig.class);

            validate(config);

            return config;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Config file has invalid JSON format: " + e.getMessage());
        }
    }

    private void validate(AppConfig config) {
        if (config == null) {
            throw new RuntimeException("Config file is empty");
        }
        if (config.getSecret() == null || config.getSecret().isBlank()) {
            throw new RuntimeException("Config error: 'secret' is missing or empty");
        }
        if (config.getHmacAlg() == null) {
            throw new RuntimeException("Config error: 'hmacAlg' is missing");
        }
        if (config.getListenPort() <= 0) {
            throw new RuntimeException("Config error: 'listenPort' must be a positive integer");
        }
        if (config.getMaxMsgSizeBytes() == null || config.getMaxMsgSizeBytes() <= 0) {
            throw new RuntimeException("Config error: 'maxMsgSizeBytes' must be a positive long");
        }
    }
}