package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.post.Comment;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Setter
@Getter
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private UserProfile author;
    private int likeCount;
    private int viewCount;
    private List<CommentDto> comments;

    @Builder
    public PostDto(Long id, String title, String content, LocalDateTime createdAt, UserProfile author, int likeCount, int viewCount, List<Comment> comments) {

        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.comments = comments.stream().map(Comment::toDto).collect(Collectors.toList());
    }
}
