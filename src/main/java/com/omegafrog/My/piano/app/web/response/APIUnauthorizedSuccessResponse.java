package com.omegafrog.My.piano.app.web.response;

import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 401 unauthorized
 */
public class APIUnauthorizedSuccessResponse extends JsonAPISuccessResponse {
    public APIUnauthorizedSuccessResponse(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public APIUnauthorizedSuccessResponse(String message, Map<String, Object> serializedData) {
        super(HttpStatus.UNAUTHORIZED.value(), message, serializedData);
    }
}
