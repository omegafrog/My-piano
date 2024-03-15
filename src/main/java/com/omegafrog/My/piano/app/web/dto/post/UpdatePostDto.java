package com.omegafrog.My.piano.app.web.dto.post;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class UpdatePostDto {
    private String title;
    private String content;

    @Builder
    public UpdatePostDto(  String title, String content) {
        this.title = title;
        this.content = content;
    }
}
