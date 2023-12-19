package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.dto.RegisterArticleDto;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Setter
@Getter
@NoArgsConstructor
public class PostRegisterDto extends RegisterArticleDto {
    @Builder
    public PostRegisterDto(String title, String content) {
        super(title,content);
    }
}
