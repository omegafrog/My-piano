package com.omegafrog.My.piano.app.dto;

import com.omegafrog.My.piano.app.post.entity.Comment;
import com.omegafrog.My.piano.app.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    private User author;
    private LocalDateTime createdAt;
    private int viewCount;
    private String title;
    private String content;
    private int likeCount;
    private List<Comment> comments;

}
