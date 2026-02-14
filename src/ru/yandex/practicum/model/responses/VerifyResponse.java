package ru.yandex.practicum.model.responses;

@SuppressWarnings("unused")
public class VerifyResponse {
    private boolean isValid;

    public VerifyResponse(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
