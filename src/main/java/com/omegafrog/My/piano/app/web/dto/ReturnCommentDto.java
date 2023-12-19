package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class ReturnCommentDto {
    private Long id;
    private UserProfile author;
    private LocalDateTime createdAt;
    private String content;
    private int likeCount;
    private Long targetId;

    @Builder
    public ReturnCommentDto(Long id, UserProfile author, LocalDateTime createdAt, String content, int likeCount, Long targetId) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.content = content;
        this.likeCount = likeCount;
        this.targetId = targetId;
    }
}
