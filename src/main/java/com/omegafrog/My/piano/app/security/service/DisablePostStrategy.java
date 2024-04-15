package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisablePostStrategy implements UpdatePostStrategy {

    public static final String optionName = "disable";
    private final Boolean optionValue;
    @Override
    public void update(Post post) {
        if(optionValue) post.disable();
        else post.enable();

    }
}
