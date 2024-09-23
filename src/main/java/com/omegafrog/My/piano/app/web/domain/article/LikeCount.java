package com.omegafrog.My.piano.app.web.domain.article;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public abstract class LikeCount {
    @Id
    protected Long id;
    @Setter
    protected int likeCount;
    public static final String HASH_KEY_NAME = "likeCount";

    public LikeCount(Long id, int likeCount) {
        this.likeCount = likeCount;
        this.id = id;
    }
}
