package com.omegafrog.My.piano.app.utils.response;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@NoArgsConstructor
public class  APIRedirectResponse<T> extends JsonAPIResponse<T> {

    private String redirect_url;

    public APIRedirectResponse( String message, String redirect_url, T data) {
        super(302, message, data);
        this.redirect_url = redirect_url;
    }
}
