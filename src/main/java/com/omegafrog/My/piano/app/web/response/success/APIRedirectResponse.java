package com.omegafrog.My.piano.app.web.response.success;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;

import java.net.URI;

@Getter
public class APIRedirectResponse<T> extends JsonAPIResponse<T> {
    @Builder
    public APIRedirectResponse(String message, HttpHeaders headers) {
        super(headers, 302, message);
    }
}
