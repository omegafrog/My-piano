package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.post.Post;

import java.time.LocalDateTime;

public record PostListDto(Long id,String title, String authorName, boolean disabled, LocalDateTime createdAt, int viewCount) {

    public PostListDto(Post post){
        this(post.getId(),post.getTitle(), post.getAuthor().getName(), post.isDisabled(),post.getCreatedAt(), post.getViewCount());
    }
}
