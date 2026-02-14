package ru.yandex.practicum.model;

@SuppressWarnings("unused")
public class ApiError {
    private String error;

    public ApiError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}