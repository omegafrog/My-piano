package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APIForbiddenSuccessResponse extends JsonAPISuccessResponse {
    public APIForbiddenSuccessResponse(String message, Map<String, Object> serializedData) {
        super(HttpStatus.FORBIDDEN.value(), message, serializedData);
    }

    public APIForbiddenSuccessResponse(String message) {
        super(HttpStatus.FORBIDDEN.value(), message);
    }
}
