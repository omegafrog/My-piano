package com.omegafrog.My.piano.app.web.dto.post;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class PostRegisterDto {
    private String title;
    private String content;

    @Builder
    public PostRegisterDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
