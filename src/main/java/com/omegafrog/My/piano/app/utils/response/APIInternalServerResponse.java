package com.omegafrog.My.piano.app.utils.response;

import org.springframework.http.HttpStatus;

public class APIInternalServerResponse extends JsonAPIResponse {
    public APIInternalServerResponse( String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.toString(), message);
    }
}
