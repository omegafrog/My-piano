package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class ReturnCommentDto {
    private Long id;
    private UserInfo author;
    private LocalDateTime createdAt;
    private String content;
    private int likeCount;
    private Long targetId;

    @Builder
    public ReturnCommentDto(Long id, UserInfo author, LocalDateTime createdAt, String content, int likeCount, Long targetId) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.content = content;
        this.likeCount = likeCount;
        this.targetId = targetId;
    }
}
