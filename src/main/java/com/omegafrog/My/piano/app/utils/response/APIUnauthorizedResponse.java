package com.omegafrog.My.piano.app.utils.response;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class APIUnauthorizedResponse extends JsonAPIResponse {
    public APIUnauthorizedResponse(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public APIUnauthorizedResponse( String message, Map<String, Object> serializedData) {
        super(HttpStatus.UNAUTHORIZED.value(), message, serializedData);
    }
}
