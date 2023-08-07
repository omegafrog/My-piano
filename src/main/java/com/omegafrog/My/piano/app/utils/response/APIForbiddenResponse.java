package com.omegafrog.My.piano.app.utils.response;

import org.springframework.http.HttpStatus;

public class APIForbiddenResponse extends JsonAPIResponse {
    public APIForbiddenResponse(String message, String serializedData) {
        super(HttpStatus.FORBIDDEN.toString(), message, serializedData);
    }

    public APIForbiddenResponse(String message) {
        super(HttpStatus.FORBIDDEN.toString(), message);
    }
}
