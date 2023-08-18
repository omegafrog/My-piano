package com.omegafrog.My.piano.app.web.dto;

import lombok.*;

@Data
@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
public abstract class RegisterArticleDto {
    private String title;
    private String content;

    protected RegisterArticleDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
