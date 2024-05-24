package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SheetPostViewCountImpl implements SheetPostViewCountRepository {

    private final JpaSheetPostViewCountRepository repository;
    private final RedisTemplate<String, SheetPostViewCount> redisTemplate;

    @Override
    public int incrementViewCount(SheetPost sheetPost) {
        if(!exist(sheetPost.getId())) {
            SheetPostViewCount saved = save(SheetPostViewCount.builder()
                    .id(sheetPost.getId())
                    .viewCount(sheetPost.getViewCount() + 1).build());
            return saved.getViewCount();
        }
        return redisTemplate.opsForHash().increment(SheetPostViewCount.KEY_NAME+":"+ sheetPost.getId(),
                "viewCount", 1L).intValue();
    }

    @Override
    public SheetPostViewCount findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find sheet post view count entity : " + id));
    }

    @Override
    public boolean exist(Long id) {
        return redisTemplate.opsForHash().hasKey(SheetPostViewCount.KEY_NAME+":"+id, "viewCount");
    }

    @Override
    public SheetPostViewCount save(SheetPostViewCount viewCount) {
        return repository.save(viewCount);
    }
}
