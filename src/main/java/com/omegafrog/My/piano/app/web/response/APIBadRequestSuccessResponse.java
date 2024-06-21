package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import org.springframework.http.HttpStatus;

public class APIBadRequestSuccessResponse extends JsonAPISuccessResponse<Void> {
    public APIBadRequestSuccessResponse(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }
}
