package com.omegafrog.My.piano.app.utils.response;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class JsonAPIResponse {
    protected int status;
    protected String message;
    protected Map<String, Object> serializedData;

    public JsonAPIResponse(int status, String message, Map<String, Object> serializedData) {
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
