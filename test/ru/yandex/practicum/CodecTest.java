package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.utils.Codec;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CodecTest {
    @Test
    void codec_ShouldEncodeAndDecodeCorrectly() {
        Codec codec = new Codec();
        String original = "TestString_123";
        byte[] bytes = original.getBytes(StandardCharsets.UTF_8);

        String encoded = codec.encode(bytes);
        assertNotNull(encoded);

        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
        assertFalse(encoded.endsWith("="));

        byte[] decoded = codec.decode(encoded);
        assertArrayEquals(bytes, decoded);
    }

    @Test
    void codec_ShouldThrowOnInvalidBase64() {
        Codec codec = new Codec();
        assertThrows(IllegalArgumentException.class, () -> codec.decode("@@@InvalidChars%%%"));
    }
}
