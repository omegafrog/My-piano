package com.omegafrog.My.piano.app.utils.response;

import org.springframework.http.HttpStatus;

public class APIUnauthorizedResponse extends JsonAPIResponse {
    public APIUnauthorizedResponse(String message) {
        super(HttpStatus.UNAUTHORIZED.toString(), message);
    }

    public APIUnauthorizedResponse( String message, String serializedData) {
        super(HttpStatus.UNAUTHORIZED.toString(), message, serializedData);
    }
}
