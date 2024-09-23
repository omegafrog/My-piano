package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("sheetpost")
@NoArgsConstructor
@Getter
public class SheetPostViewCount extends ViewCount {
    public static String KEY_NAME="sheetpost";

    @Builder
    public SheetPostViewCount(Long id,int viewCount) {
        super(viewCount);
        this.id = id;
    }

}
