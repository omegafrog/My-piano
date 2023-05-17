package com.omegafrog.My.piano.app.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class WritePostDto {
    private LocalDateTime createdAt;
    private int viewCount;
    private String title;
    private String content;
    private int likeCount;

    @Builder
    public WritePostDto(LocalDateTime createdAt, String title, String content) {
        this.createdAt = createdAt;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
        this.viewCount = 0;
    }
}
