package com.omegafrog.My.piano.app.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentDTO {
    private String content;

    @Builder
    public CommentDTO(String content) {
        this.content = content;
    }
}
