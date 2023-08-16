package com.omegafrog.My.piano.app.web.dto.videoPost;

import com.omegafrog.My.piano.app.web.dto.ArticleDto;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Service
@NoArgsConstructor
public class VideoPostDto extends ArticleDto {

    private String videoUrl;

    @Builder
    public VideoPostDto(Long id, String title, String content, LocalDateTime createdAt, UserProfile author, int likeCount, int viewCount, List<CommentDto> comments, String videoUrl) {
        super(id, title, content, createdAt, author, likeCount, viewCount, comments);
        this.videoUrl = videoUrl;
    }
}
