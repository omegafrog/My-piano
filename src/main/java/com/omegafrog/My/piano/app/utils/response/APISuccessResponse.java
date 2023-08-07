package com.omegafrog.My.piano.app.utils.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APISuccessResponse extends JsonAPIResponse {


    public APISuccessResponse(String message, @NotNull Map<String, Object> data, ObjectMapper objectMapper)
            throws JsonProcessingException, NullPointerException {
        super(String.valueOf(HttpStatus.OK), message);
        String s = objectMapper.writeValueAsString(data);
        s = s.replaceAll("\\\\", "");
        this.serializedData = s;
    }

    public APISuccessResponse(String message) {
        super(String.valueOf(HttpStatus.OK), message);
    }
}
