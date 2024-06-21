package com.omegafrog.My.piano.app.web.response.success;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Getter
public class APIRedirectSuccessResponse<T> extends JsonAPISuccessResponse<T> {


    @Builder
    public APIRedirectSuccessResponse(String message, String redirect_url, T data) {
        super(302, message, data);
        this.getHeaders().setLocation(URI.create(redirect_url));
    }
}
