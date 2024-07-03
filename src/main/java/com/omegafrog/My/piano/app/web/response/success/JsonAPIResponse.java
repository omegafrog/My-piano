package com.omegafrog.My.piano.app.web.response.success;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public abstract class JsonAPIResponse<T> extends ResponseEntity<JsonAPIResponse.ResponseBody<T>> {

    @AllArgsConstructor
    @Getter
    public static class ResponseBody<T>{
        private String message;
        private int status;
        private T data;
    }

    public JsonAPIResponse(int status, String message, T data) {
        super(new ResponseBody(message, status, data), HttpStatus.valueOf(status)) ;
    }
    public JsonAPIResponse(int status, String message) {
        super(new ResponseBody<>(message, status, null), HttpStatus.valueOf(status));
    }
    public JsonAPIResponse(HttpHeaders headers, int status, String message ){
        super(new ResponseBody<>(message, status, null),headers, HttpStatus.valueOf(status));
    }
}
