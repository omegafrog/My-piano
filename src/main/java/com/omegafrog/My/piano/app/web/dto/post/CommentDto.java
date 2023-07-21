package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.post.Comment;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private UserProfile author;
    private LocalDateTime createdAt;

    private String content;

    private int likeCount;

    private List<Comment> replies;

    @Builder
    public CommentDto(Long id, UserProfile author, LocalDateTime createdAt, String content, int likeCount, List<Comment> replies) {
        this.id = id;
        this.author = author;
        this.createdAt = createdAt;
        this.content = content;
        this.likeCount = likeCount;
        this.replies = replies;
    }
}
