package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SheetPostViewCountRepositoryImpl implements SheetPostViewCountRepository {

    private final RedisSheetPostViewCountRepository repository;
    private final RedisTemplate<String, SheetPostViewCount> redisTemplate;

    @Override
    public int incrementViewCount(SheetPost sheetPost) {
        if (!exist(sheetPost.getId())) {
            SheetPostViewCount saved = save(SheetPostViewCount.builder()
                    .id(sheetPost.getId())
                    .viewCount(sheetPost.getViewCount()).build());
            return redisTemplate.opsForHash().increment(SheetPostViewCount.KEY_NAME + ":" + sheetPost.getId(),
                    "viewCount", 1L).intValue();
        }
        return redisTemplate.opsForHash().increment(SheetPostViewCount.KEY_NAME + ":" + sheetPost.getId(),
                "viewCount", 1L).intValue();
    }

    @Override
    public SheetPostViewCount findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Cannot find sheet post view count entity : " + id));
    }

    @Override
    public boolean exist(Long id) {
        return redisTemplate.opsForHash().hasKey(SheetPostViewCount.KEY_NAME + ":" + id, "viewCount");
    }

    @Override
    public SheetPostViewCount save(SheetPostViewCount viewCount) {
        redisTemplate.opsForHash().put(SheetPostViewCount.KEY_NAME + ":" + viewCount.getId(),
                "viewCount", String.valueOf(viewCount.getViewCount()));
        return viewCount;
    }

    @Override
    public Map<Long, Integer> getViewCountsByIds(List<Long> ids) {
        Map<Long, Integer> viewCounts = new HashMap<>();
        
        for (Long id : ids) {
            String key = SheetPostViewCount.KEY_NAME + ":" + id;
            Object viewCountObj = redisTemplate.opsForHash().get(key, "viewCount");
            
            if (viewCountObj != null) {
                try {
                    int viewCount = Integer.parseInt(viewCountObj.toString());
                    viewCounts.put(id, viewCount);
                } catch (NumberFormatException e) {
                    viewCounts.put(id, 0);
                }
            } else {
                viewCounts.put(id, 0);
            }
        }
        
        return viewCounts;
    }
}
