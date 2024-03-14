package com.omegafrog.My.piano.app.utils.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APISuccessResponse<T> extends JsonAPIResponse<T> {


    public APISuccessResponse(String message, @NotNull T data)
            throws JsonProcessingException, NullPointerException {
        super(HttpStatus.OK.value(), message, data);
    }

    public APISuccessResponse(String message) {
        super(HttpStatus.OK.value(), message);
    }
}
