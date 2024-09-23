package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("SheetPostLikeCountRepository")
@RequiredArgsConstructor
public class SheetPostLikeCountRepositoryImpl implements LikeCountRepository {
    private final RedisTemplate<String, SheetPostLikeCount> redisTemplate;
    private final RedisSheetPostLikeCountRepository jpaRepository;
    private final JpaSheetPostRepositoryImpl sheetPostRepository;

    @Override
    public LikeCount save(LikeCount sheetPostLikeCount) {
        redisTemplate.opsForHash().put(SheetPostLikeCount.KEY_NAME + ":" + sheetPostLikeCount.getId(),
                "likeCount", String.valueOf(sheetPostLikeCount.getLikeCount()));
        return sheetPostLikeCount;
    }

    @Override
    public SheetPostLikeCount findById(Long id) {
        // redis에 존재하지 않으면 db에서 가져와 저장
        if (!exist(id)) {
            SheetPost sheetPost = sheetPostRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post."));
            return (SheetPostLikeCount) save(new SheetPostLikeCount(id, sheetPost.getLikeCount()));
        }
        // redis에 존재할 경우 가져와서 반환
        return jpaRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Cannot find sheet like count."));
    }


    @Override
    public int incrementLikeCount(Article sheetPost) {
        if (!exist(sheetPost.getId())) {
            SheetPostLikeCount saved = (SheetPostLikeCount) save(SheetPostLikeCount.builder()
                    .id(sheetPost.getId())
                    .likeCount(sheetPost.getLikeCount() + 1).build());
            return saved.getLikeCount();
        }
        return redisTemplate.opsForHash().increment(SheetPostLikeCount.KEY_NAME + ":" + sheetPost.getId(),
                "likeCount", 1L).intValue();
    }

    @Override
    public boolean exist(Long id) {
        return redisTemplate.opsForHash().hasKey(SheetPostLikeCount.KEY_NAME + ":" + id, "likeCount");
    }

    @Override
    public int decrementLikeCount(Article sheetPost) {
        redisTemplate.opsForHash().increment(SheetPostLikeCount.KEY_NAME + ":" + sheetPost.getId(),
                "likeCount", -1L);
        return sheetPost.getLikeCount() - 1;
    }

}
