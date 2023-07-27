package com.omegafrog.My.piano.app.web.dto.post;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
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