package com.omegafrog.My.piano.app.web.dto.videoPost;

import com.omegafrog.My.piano.app.web.dto.RegisterArticleDto;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class VideoPostRegisterDto extends RegisterArticleDto {

    private String videoUrl;

    @Builder
    public VideoPostRegisterDto(String title, String content, String videoUrl) {
        super(title, content);
        this.videoUrl = videoUrl;
    }
}
