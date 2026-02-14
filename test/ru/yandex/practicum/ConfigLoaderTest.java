package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.config.ConfigLoader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigLoaderTest {

    @Test
    void configLoadingWithMissingFile() {
        ConfigLoader config = new ConfigLoader();

        Exception exception = assertThrows(IOException.class, () -> config.load("non_existent_file.json"));

        assertTrue(exception.getMessage().contains("non_existent_file.json") ||
                exception instanceof java.io.FileNotFoundException);
    }
}
