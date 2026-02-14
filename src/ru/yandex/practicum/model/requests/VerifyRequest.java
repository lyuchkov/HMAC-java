package ru.yandex.practicum.model.requests;

@SuppressWarnings("unused")
public class VerifyRequest {
    private String msg;
    private String signature;

    public VerifyRequest(String msg, String signature) {
        this.msg = msg;
        this.signature = signature;
    }

    public String getMsg() {
        return msg;
    }

    public String getSignature() {
        return signature;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
