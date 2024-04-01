package com.omegafrog.My.piano.app.utils.response;

import org.springframework.http.HttpStatus;

public class APIBadRequestResponse extends JsonAPIResponse<Void> {
    public APIBadRequestResponse(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }
}
