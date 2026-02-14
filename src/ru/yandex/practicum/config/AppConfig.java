package ru.yandex.practicum.config;

public class AppConfig {
    private final String hmacAlg;
    private String secret;
    private final int listenPort;
    private final Long maxMsgSizeBytes;

    public AppConfig(String hmacAlg, String secret, int listenPort, Long maxMsgSizeBytes) {
        this.hmacAlg = hmacAlg;
        this.secret = secret;
        this.listenPort = listenPort;
        this.maxMsgSizeBytes = maxMsgSizeBytes;
    }

    public String getHmacAlg() {
        return hmacAlg;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getListenPort() {
        return listenPort;
    }

    public Long getMaxMsgSizeBytes() {
        return maxMsgSizeBytes;
    }

}
