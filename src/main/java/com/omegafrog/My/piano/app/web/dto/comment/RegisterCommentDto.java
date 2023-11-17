package com.omegafrog.My.piano.app.web.dto.comment;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
public class RegisterCommentDto {
    private String content;

    @Builder
    public RegisterCommentDto(String content) {
        this.content = content;
    }
}
