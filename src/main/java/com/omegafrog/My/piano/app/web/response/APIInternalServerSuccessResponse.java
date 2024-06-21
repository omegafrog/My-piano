package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import org.springframework.http.HttpStatus;

public class APIInternalServerSuccessResponse extends JsonAPISuccessResponse {
    public APIInternalServerSuccessResponse(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
