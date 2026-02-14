package ru.yandex.practicum.server;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public ApiException(int statusCode, String errorCode) {
        super(errorCode);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
}