package com.omegafrog.My.piano.app.response;

public abstract class JsonAPIResponse {
    private String status;
    private String message;

    public JsonAPIResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
