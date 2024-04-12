package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ArticleDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
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
    public PostDto(Long id, String title, String content, LocalDateTime createdAt, UserProfileDto author, int likeCount,
                   int viewCount, List<Comment> comments, Boolean disable) {
        super(id, title, content, createdAt, author, likeCount, viewCount, comments.stream().map(Comment::toDto).toList(),
                disable);
    }

    public PostDto(Post founded, User author) {
        super(founded.getId(), founded.getTitle(), founded.getContent(), founded.getCreatedAt(),
                author.getUserProfileDto(), founded.getLikeCount(), founded.getViewCount(),
                founded.getComments().stream().map(Comment::toDto).toList(), founded.isDisabled());
    }
}
