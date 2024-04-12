package com.omegafrog.My.piano.app.web.dto.videoPost;

import com.omegafrog.My.piano.app.web.dto.ArticleDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
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
    public VideoPostDto(Long id, String title, String content, LocalDateTime createdAt, UserProfileDto author, int likeCount, int viewCount, List<CommentDto> comments, String videoUrl,
                        boolean disabled) {
        super(id, title, content, createdAt, author, likeCount, viewCount, comments, disabled);
        this.videoUrl = videoUrl;
    }
}
