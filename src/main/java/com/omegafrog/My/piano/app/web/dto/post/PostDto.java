package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.dto.ArticleDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Setter
@Getter
@NoArgsConstructor
public class PostDto extends ArticleDto {

    @Builder
    public PostDto(Long id, String title, String content, LocalDateTime createdAt, UserInfo author, int likeCount, int viewCount, List<Comment> comments) {
        super(id, title, content, createdAt, author, likeCount, viewCount, comments.stream().map(Comment::toDto).toList());
    }
}
