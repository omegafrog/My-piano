package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public abstract class ArticleDto {
    @NotNull
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private String content;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private UserProfileDto author;
    @NotNull
    private int likeCount;
    @NotNull
    private int viewCount;
    @NotNull
    private List<CommentDto> comments;
    private boolean disabled = false;

    protected ArticleDto(Long id, String title, String content, LocalDateTime createdAt, UserProfileDto author, int likeCount, int viewCount, List<CommentDto> comments,
                         boolean disabled) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.comments = comments;
        this.disabled = disabled;
    }
}
