package ru.yandex.practicum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.service.HmacService;

import static org.junit.jupiter.api.Assertions.*;

public class HmacServiceTest {
    private static final String SECRET = "testSecret";
    private static final String ALGORITHM = "HmacSHA256";

    @Test
    @DisplayName("Стабильность кодирования. Подписи детерминированы: одинаковый msg → одинаковая signature (при неизменном секрете).")
    void serviceDeterministicTest() {
        HmacService service = new HmacService(SECRET, ALGORITHM);
        String msg = "Hello World";
        String sig1 = service.sign(msg);
        String sig2 = service.sign(msg);

        assertEquals(sig1, sig2, "Подпись должна быть одинаковой для одного msg и secret");
    }


    @Test
    void servicePositiveSmokeTest() {
        HmacService service = new HmacService(SECRET, ALGORITHM);
        String msg = "Hello";
        String signature = service.sign(msg);

        assertTrue(service.verify(msg, signature));
    }

    @Test
    void serviceNegativeSmokeTest() {
        HmacService service = new HmacService(SECRET, ALGORITHM);
        String msg = "Hello";
        String signature = service.sign(msg);

        assertFalse(service.verify(msg+"asdasasf", signature));
    }

    @Test
    void serviceThrowForInvalidBase64SignatureTest() {
        HmacService service = new HmacService(SECRET, ALGORITHM);

        assertThrows(IllegalArgumentException.class, () -> service.verify("msg", "@@@"));
    }

    @Test
    void serviceConstructorShouldThrowOnEmptySecretTest() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService("", ALGORITHM));
    }

    @Test
    void serviceConstructorShouldThrowOnEmptyAlgorithmTest() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService(SECRET,""));
    }

    @Test
    void serviceConstructorShouldThrowOnNullSecretTest() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService(null, ALGORITHM));
    }

    @Test
    void serviceConstructorShouldThrowOnNullAlgorithmTest() {
        assertThrows(IllegalArgumentException.class, () -> new HmacService(SECRET,null));
    }
}