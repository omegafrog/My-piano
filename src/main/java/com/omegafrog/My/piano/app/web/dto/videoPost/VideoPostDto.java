package com.omegafrog.My.piano.app.web.dto.videoPost;

import com.omegafrog.My.piano.app.web.dto.ArticleDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Service
@NoArgsConstructor
public class VideoPostDto extends ArticleDto {

    private String videoUrl;

    @Builder
    public VideoPostDto(Long id, String title, String content, LocalDateTime createdAt, UserProfileDto author, int likeCount, int viewCount, String videoUrl,
                        boolean disabled) {
        super(id, title, content, createdAt, author, likeCount, viewCount, disabled);
        this.videoUrl = videoUrl;
    }
}
