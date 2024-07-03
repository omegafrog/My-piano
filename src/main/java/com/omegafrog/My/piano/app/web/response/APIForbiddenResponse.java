package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APIForbiddenResponse extends JsonAPIResponse {
    public APIForbiddenResponse(String message, Map<String, Object> serializedData) {
        super(HttpStatus.FORBIDDEN.value(), message, serializedData);
    }

    public APIForbiddenResponse(String message) {
        super(HttpStatus.FORBIDDEN.value(), message);
    }
}
