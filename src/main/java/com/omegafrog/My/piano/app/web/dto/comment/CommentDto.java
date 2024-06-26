package com.omegafrog.My.piano.app.web.dto.comment;

import com.omegafrog.My.piano.app.web.dto.user.UserInfo;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private UserInfo author;
    private LocalDateTime createdAt;

    private String content;

    private int likeCount;

    private List<CommentDto> replies;

    @Builder
    public CommentDto(Long id, UserInfo author, LocalDateTime createdAt, String content, int likeCount, List<CommentDto> replies) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.content = content;
        this.likeCount = likeCount;
        this.replies = replies;
    }
}
