package com.omegafrog.My.piano.app.web.response.success;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

public class ApiSuccessResponse<T> extends JsonAPISuccessResponse<T> {
    public ApiSuccessResponse(String message, @NotNull T data){
        super(HttpStatus.OK.value(), message, data);
    }

    public ApiSuccessResponse(String message) {
        super(HttpStatus.OK.value(), message);
    }
}
