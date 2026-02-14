package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.config.ConfigLoader;
import ru.yandex.practicum.server.SignHandler;
import ru.yandex.practicum.server.VerifyHandler;
import ru.yandex.practicum.service.HmacService;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

public class ServerHMAC {
    private static final String CONFIG_PATH = "src/resources/config.json";
    private static final Logger logger = Logger.getLogger("ServerHMAC");

    public static void main(String[] args) {
        if (args.length > 0 && "rotate-secret".equals(args[0])) {
            rotateSecret();
            return;
        }

        startServer();
    }

    private static void startServer() {
        try {
            ConfigLoader loader = new ConfigLoader();
            AppConfig config = loader.load(CONFIG_PATH);

            if (config.getSecret() == null || config.getSecret().isEmpty()) {
                logger.severe("Config error: Secret is missing");
                System.exit(1);
            }

            HmacService hmacService = new HmacService(config.getSecret(), config.getHmacAlg());
            HttpServer server = HttpServer.create(new InetSocketAddress(config.getListenPort()), 0);

            server.createContext("/sign", new SignHandler(hmacService, config));
            server.createContext("/verify", new VerifyHandler(hmacService, config));

            server.setExecutor(null);
            server.start();

            logger.info("Server started on port " + config.getListenPort());

        } catch (Exception e) {
            logger.severe("Startup failed: " + e.getMessage());
        }
    }

    private static void rotateSecret() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            AppConfig config;

            try (FileReader reader = new FileReader(CONFIG_PATH)) {
                config = gson.fromJson(reader, AppConfig.class);
            }

            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String newSecret = Base64.getEncoder().encodeToString(bytes);

            config.setSecret(newSecret);

            try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
                gson.toJson(config, writer);
            }

            System.out.println("Secret rotated successfully. New secret stored in config.json.");

        } catch (IOException e) {
            System.err.println("Failed to rotate secret: " + e.getMessage());
            System.exit(1);
        }
    }
}
