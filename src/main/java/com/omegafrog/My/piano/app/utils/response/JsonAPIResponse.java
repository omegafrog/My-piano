package com.omegafrog.My.piano.app.utils.response;

import lombok.Getter;

@Getter
public abstract class JsonAPIResponse {
    protected int status;
    protected String message;
    protected String serializedData;

    public JsonAPIResponse(int status, String message, String serializedData) {
        this.status = status;
        this.message = message;
        this.serializedData = serializedData;
    }
    public JsonAPIResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.serializedData = null;
    }
}
