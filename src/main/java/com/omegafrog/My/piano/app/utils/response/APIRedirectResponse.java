package com.omegafrog.My.piano.app.utils.response;

import java.util.Map;

public class APIRedirectResponse extends JsonAPIResponse {

    private String redirect_url;

    public APIRedirectResponse( String message, String redirect_url, Map<String, Object> data) {
        super(302, message, data);
        this.redirect_url = redirect_url;
    }
}
