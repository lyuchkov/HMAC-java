package ru.yandex.practicum.model.responses;

@SuppressWarnings("unused")
public class SignResponse {
    private String signature;

    public SignResponse(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
