package ru.yandex.practicum.service;

import ru.yandex.practicum.utils.Codec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HmacService {
    private final byte[] secretKeyBytes;
    private final String algorithm;
    private final Codec codec;

    public HmacService(String secret, String algorithm) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be empty");
        }
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.algorithm = algorithm;
        this.codec = new Codec();
    }

    public String sign(String msg) {
        byte[] hmacBytes = calculateHmac(msg);
        return codec.encode(hmacBytes);
    }

    public boolean verify(String msg, String receivedSignature) {
        byte[] receivedBytes = codec.decode(receivedSignature);

        byte[] expectedBytes = calculateHmac(msg);

        return MessageDigest.isEqual(expectedBytes, receivedBytes);
    }

    private byte[] calculateHmac(String msg) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secretKeyBytes, algorithm));
            return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Hmac error", e);
        }
    }
}