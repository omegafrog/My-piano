package com.omegafrog.My.piano.app.utils.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public abstract class JsonAPIResponse<T> {
    protected int status;
    protected String message;
    protected T data;

    public JsonAPIResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public JsonAPIResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }
}
