package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("sheetpost")
@NoArgsConstructor
@Getter
public class SheetPostLikeCount extends LikeCount {
    public static String KEY_NAME = "sheetpost";

    @Builder
    public SheetPostLikeCount(Long id, int likeCount) {
        super(id, likeCount);
    }

    public static SheetPostLikeCount of(LikeCount sheetPostLikeCount) {
        return SheetPostLikeCount.builder()
                .id(sheetPostLikeCount.getId())
                .likeCount(sheetPostLikeCount.getLikeCount())
                .build();
    }
}
