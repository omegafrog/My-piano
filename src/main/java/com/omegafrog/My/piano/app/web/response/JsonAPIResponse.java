package com.omegafrog.My.piano.app.web.response;

import lombok.Getter;

@Getter
public abstract class JsonAPIResponse {
    protected String status;
    protected String message;
    protected String serializedData;

    public JsonAPIResponse(String status, String message, String serializedData) {
        this.status = status;
        this.message = message;
        this.serializedData = serializedData;
    }
    public JsonAPIResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.serializedData = null;
    }
}
