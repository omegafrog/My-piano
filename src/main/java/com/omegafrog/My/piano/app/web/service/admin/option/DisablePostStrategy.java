package com.omegafrog.My.piano.app.web.service.admin.option;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisablePostStrategy implements PostStrategy {

    public static final String OPTION_NAME = "disable";
    private final boolean optionValue;
    @Override
    public void update(Post post) {
        if(optionValue) post.disable();
        else post.enable();

    }
}
