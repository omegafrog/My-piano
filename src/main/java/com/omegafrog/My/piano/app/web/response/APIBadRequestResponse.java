package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import org.springframework.http.HttpStatus;

public class APIBadRequestResponse extends JsonAPIResponse<Void> {
    public APIBadRequestResponse(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }
}
