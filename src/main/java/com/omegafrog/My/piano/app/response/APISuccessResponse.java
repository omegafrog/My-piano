package com.omegafrog.My.piano.app.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APISuccessResponse extends JsonAPIResponse {
    public APISuccessResponse(String message, ObjectMapper objectMapper, Map<String, Object> data) throws JsonProcessingException {
        super(String.valueOf(HttpStatus.OK), message);
        this.serializedData = objectMapper.writeValueAsString(data);
    }
}