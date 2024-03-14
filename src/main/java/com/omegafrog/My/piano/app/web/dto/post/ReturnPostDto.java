package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;

import java.time.LocalDateTime;
import java.util.List;

public record ReturnPostDto(Long id, String title, String content, UserProfileDto author, LocalDateTime createdAt,
                            Integer likeCount, Integer viewCount, List<Comment> comments) {
    public ReturnPostDto(Post post, User author){
        this(post.getId(), post.getTitle(), post.getContent(), new UserProfileDto(author.getSecurityUser().getUsername(),
                author.getProfileSrc()), post.getCreatedAt(), post.getLikeCount(), post.getViewCount(), post.getComments());
    }
}
