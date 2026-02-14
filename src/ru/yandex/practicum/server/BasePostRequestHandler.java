package ru.yandex.practicum.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.model.ApiError;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BasePostRequestHandler<T> implements HttpHandler {
    protected final Gson gson = new Gson();
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Class<T> requestType;
    protected final AppConfig config;

    public BasePostRequestHandler(Class<T> requestType, AppConfig config) {
        this.requestType = requestType;
        this.config = config;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                sendError(exchange, 415, "unsupported_media_type");
                return;
            }

            T requestBody;
            try {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                requestBody = gson.fromJson(reader, requestType);
            } catch (JsonSyntaxException e) {
                sendError(exchange, 400, "invalid_json");
                return;
            }

            if (requestBody == null) {
                sendError(exchange, 400, "invalid_json");
                return;
            }

            Object response = process(requestBody);

            sendJson(exchange, 200, response);

        } catch (ApiException e) {
            logger.warning("API Error: " + e.getErrorCode() + " - " + e.getMessage());
            sendError(exchange, e.getStatusCode(), e.getErrorCode());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Internal Error", e);
            sendError(exchange, 500, "internal");
        }
    }

    protected abstract Object process(T request) throws ApiException;

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = gson.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String errorCode) throws IOException {
        sendJson(exchange, statusCode, new ApiError(errorCode));
    }
}