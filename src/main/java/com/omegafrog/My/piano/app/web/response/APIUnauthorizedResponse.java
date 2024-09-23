package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 401 unauthorized
 */
public class APIUnauthorizedResponse extends JsonAPIResponse {
    public APIUnauthorizedResponse(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public APIUnauthorizedResponse(String message, Map<String, Object> serializedData) {
        super(HttpStatus.UNAUTHORIZED.value(), message, serializedData);
    }
}
