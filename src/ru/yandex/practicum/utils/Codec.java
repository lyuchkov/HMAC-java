package ru.yandex.practicum.utils;

import java.util.Base64;

public class Codec {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    public String encode(byte[] data) {
        if (data == null) return null;
        return ENCODER.encodeToString(data);
    }


    public byte[] decode(String data) {
        if (data == null) throw new IllegalArgumentException("Input cannot be null");
        return DECODER.decode(data);
    }
}
