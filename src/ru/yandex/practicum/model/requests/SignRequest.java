package ru.yandex.practicum.model.requests;

@SuppressWarnings("unused")
public class SignRequest {
    private String msg;

    public SignRequest(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
