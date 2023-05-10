package com.omegafrog.My.piano.app.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class APISuccessResponse extends JsonAPIResponse {
    @Autowired
    private ObjectMapper objectMapper;
    private String data;

    public APISuccessResponse( String message, Map<String, Object> data) throws JsonProcessingException {
        super(String.valueOf(HttpStatus.OK), message);
        this.data = objectMapper.writeValueAsString(data);
    }
}
