package ru.yandex.practicum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.server.SignHandler;
import ru.yandex.practicum.server.VerifyHandler;
import ru.yandex.practicum.service.HmacService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ServerHMACTest {
    private HttpServer server;
    private static final int PORT = 8080;
    private static final String SECRET = "testSecret";
    private static final String ALGORITHM = "HmacSHA256";
    private static final long MAX_MSG_SIZE = 100L;
    private HttpClient client;
    private final Gson gson = new Gson();

    @BeforeEach
    void startServer() throws IOException {
        client = HttpClient.newHttpClient();

        AppConfig config = new AppConfig(
                ALGORITHM,
                SECRET,
                PORT,
                MAX_MSG_SIZE
        );
        config.setSecret(SECRET);

        HmacService hmacService = new HmacService(SECRET, ALGORITHM);

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/sign", new SignHandler(hmacService, config));
        server.createContext("/verify", new VerifyHandler(hmacService, config));

        server.setExecutor(null);
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private HttpResponse<String> post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Подпись/проверка успеха. msg=\"hello\" → /sign → подпись → /verify → ok=true.")
    void fullSuccessFlowTest() throws Exception {
        String msg = "hello";
        String signJson = "{\"msg\": \"" + msg + "\"}";
        HttpResponse<String> signResp = post("/sign", signJson);

        assertEquals(200, signResp.statusCode());
        String signature = gson.fromJson(signResp.body(), JsonObject.class).get("signature").getAsString();
        assertNotNull(signature);

        String verifyJson = String.format("{\"msg\": \"%s\", \"signature\": \"%s\"}", msg, signature);
        HttpResponse<String> verifyResp = post("/verify", verifyJson);

        assertEquals(200, verifyResp.statusCode());
        boolean isValid = gson.fromJson(verifyResp.body(), JsonObject.class).get("isValid").getAsBoolean();
        assertTrue(isValid, "Верификация должна пройти успешно");
    }

    @Test
    @DisplayName("Неверная подпись. Изменить 1 байт в signature → ok=false.")
    void tamperedSignatureShouldReturnFalseTest() throws Exception {
        HmacService localService = new HmacService(SECRET, ALGORITHM);
        String msg = "hello";
        String validSig = localService.sign(msg);

        String tamperedSig = validSig.substring(0, validSig.length() - 1) + "0";

        String json = String.format("{\"msg\": \"%s\", \"signature\": \"%s\"}", msg, tamperedSig);
        HttpResponse<String> resp = post("/verify", json);

        assertEquals(200, resp.statusCode());
        boolean isValid = gson.fromJson(resp.body(), JsonObject.class).get("isValid").getAsBoolean();
        assertFalse(isValid, "Измененная подпись не должна проходить проверку");
    }

    @Test
    @DisplayName("Изменённое сообщение Подпись для \"hello\", проверка \"hello!\" → ok=false.")
    void tamperedMessageShouldReturnFalseTest() throws Exception {
        HmacService localService = new HmacService(SECRET, ALGORITHM);
        String originalMsg = "hello";
        String sig = localService.sign(originalMsg);

        String json = String.format("{\"msg\": \"hello!\", \"signature\": \"%s\"}", sig);
        HttpResponse<String> resp = post("/verify", json);

        assertEquals(200, resp.statusCode());
        boolean isValid = gson.fromJson(resp.body(), JsonObject.class).get("isValid").getAsBoolean();
        assertFalse(isValid, "Подпись от другого сообщения не должна подходить");
    }

    @Test
    @DisplayName("Невалидная base64url Поле signature=\"@@@\" → 400 invalid_signature_format.")
    void invalidBase64UrlShouldReturn400Test() throws Exception {
        String json = "{\"msg\": \"test\", \"signature\": \"@@@\"}";
        HttpResponse<String> resp = post("/verify", json);

        assertEquals(400, resp.statusCode());
        String error = gson.fromJson(resp.body(), JsonObject.class).get("error").getAsString();
        assertEquals("invalid_signature_format", error);
    }

    @Test
    @DisplayName("Пустой msg Поле msg=\"\" → 400 invalidMsg.")
    void emptyMessageTest() throws Exception {
        String json = "{\"msg\": \"\"}";
        HttpResponse<String> resp = post("/sign", json);

        assertEquals(400, resp.statusCode());
        String error = gson.fromJson(resp.body(), JsonObject.class).get("error").getAsString();
        assertEquals("invalid_msg", error);
    }

    @Test
    @DisplayName("Большое сообщение. Сообщение > maxMsgSizeBytes → 413.")
    void payloadTooLargeTest() throws Exception {
        String bigMsg = "a".repeat(150);
        String json = "{\"msg\": \"" + bigMsg + "\"}";

        HttpResponse<String> resp = post("/sign", json);

        assertEquals(413, resp.statusCode());
        String error = gson.fromJson(resp.body(), JsonObject.class).get("error").getAsString();
        assertEquals("payload_too_large", error);
    }

    @Test
    @DisplayName("Проверить наличие заголовка Content-Type: application/json. Если его нет, то ошибка 415.")
    void invalidContentTypeTest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/sign"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("stuff"))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(415, resp.statusCode());
    }

    @Test
    @DisplayName("Конфиг-ошибки Некорректный secret в config.json → сервер не стартует с понятной ошибкой.")
    void configErrorTest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/sign"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("stuff"))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(415, resp.statusCode());
    }
}
