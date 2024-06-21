package com.omegafrog.My.piano.app.web.response.success;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public abstract class JsonAPISuccessResponse<T> extends ResponseEntity<T> {
    protected String message;

    public JsonAPISuccessResponse(int status, String message, T data) {
        super(data, HttpStatus.valueOf(status));
        this.message = message;
    }
    public JsonAPISuccessResponse(int status, String message) {
        super(HttpStatus.valueOf(status));
        this.message = message;
    }
}
