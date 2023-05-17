package com.omegafrog.My.piano.app.response;

import org.springframework.http.HttpStatus;

public class APIForbiddenResponse extends JsonAPIResponse {

    public APIForbiddenResponse( String message) {
        super(String.valueOf(HttpStatus.FORBIDDEN), message);
    }
}
