package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import org.springframework.http.HttpStatus;

public class APIInternalServerResponse extends JsonAPIResponse {
    public APIInternalServerResponse(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
