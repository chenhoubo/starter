package com.example.starter.constants;

public enum HttpStatus {
    OK(200, "success"),
    PLAYER_REGISTER(201,"head register"),
    REDIRECT_LOGIN(202,"remote device login"),
    FAIL(400, "error"),

    ERROR(501, "error"),
    JSON_ERROR(502,"json error"),
    PARAMETER_ERROR(503,"parameter error");

    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
