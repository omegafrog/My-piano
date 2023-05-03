package com.omegafrog.My.piano.app.post.dto;

import com.omegafrog.My.piano.app.post.entity.Author;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdatePostDto {
    private int viewCount;
    private String title;
    private String content;
    private int likeCount;

    @Builder
    public UpdatePostDto( int viewCount, String title, String content, int likeCount) {
        this.viewCount = viewCount;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
    }
}
