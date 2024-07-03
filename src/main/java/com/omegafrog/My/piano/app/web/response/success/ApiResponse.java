package com.omegafrog.My.piano.app.web.response.success;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

public class ApiResponse<T> extends JsonAPIResponse<T> {
    public ApiResponse(String message, @NotNull T data){
        super(HttpStatus.OK.value(), message, data);
    }

    public ApiResponse(String message) {
        super(HttpStatus.OK.value(), message);
    }
}
