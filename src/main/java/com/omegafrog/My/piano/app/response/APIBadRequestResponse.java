package com.omegafrog.My.piano.app.response;

import org.springframework.http.HttpStatus;

public class APIBadRequestResponse extends JsonAPIResponse {
    public APIBadRequestResponse(String message) {
        super(HttpStatus.BAD_REQUEST.toString(), message);

    }
}
