package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class ViewCountPagingItemReader<ViewCount> extends AbstractPagingItemReader<ViewCount> {
    private final Class<ViewCount> targetType;
    private final RedisTemplate<String, ViewCount> redisTemplate;
    private List<String> allKeys;
    private int totalProcessed = 0;

    public ViewCountPagingItemReader(Class<ViewCount> targetType, RedisTemplate<String, ViewCount> redisTemplate) {
        this.targetType = targetType;
        this.redisTemplate = redisTemplate;
        this.allKeys = new ArrayList<>();
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();
        long startTime = System.currentTimeMillis();
        this.allKeys = getKeys();
        Collections.sort(this.allKeys);
        long endTime = System.currentTimeMillis();
        
        log.info("ViewCountPagingItemReader opened - Type: {}, Total keys found: {}, Scan time: {}ms", 
                targetType.getSimpleName(), allKeys.size(), endTime - startTime);
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        log.info("ViewCountPagingItemReader closed - Type: {}, Total processed: {}", 
                targetType.getSimpleName(), totalProcessed);
        this.allKeys.clear();
        this.totalProcessed = 0;
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new ArrayList<>();
        } else {
            results.clear();
        }
        
        int start = getPage() * getPageSize();
        int end = Math.min(allKeys.size(), (getPage() + 1) * getPageSize());
        
        if (start >= allKeys.size()) {
            return;
        }
        
        List<String> pageKeys = allKeys.subList(start, end);
        
        List<ViewCount> viewCounts = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<ViewCount> execute(RedisOperations operations) throws DataAccessException {
                List<ViewCount> items = new ArrayList<>();
                Constructor<ViewCount> constructor = getConstructor();
                
                for (String key : pageKeys) {
                    try {
                        Object viewCountValue = operations.opsForHash().get(key, "viewCount");
                        if (viewCountValue != null) {
                            int currentViewCount = Integer.parseInt(viewCountValue.toString());
                            if (currentViewCount > 0) {
                                items.add(constructor.newInstance(
                                        Long.valueOf(key.split(":")[1]),
                                        currentViewCount
                                ));
                                // Redis에는 현재 값을 유지 (0으로 초기화하지 않음)
                                totalProcessed++;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing key: " + key, e);
                    }
                }
                return items;
            }
        });
        
        results.addAll(viewCounts);
    }

    private @NotNull Constructor<ViewCount> getConstructor() {
        Constructor<ViewCount> constructor = null;
        try {
            constructor = targetType.getConstructor(Long.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return constructor;
    }

    private static final Map<Class<?>, String> TYPE_PATTERNS = Map.of(
            SheetPostViewCount.class, "sheetpost:*",
            LessonViewCount.class, "lesson:*",
            PostViewCount.class, "post:*"
    );

    private @NotNull List<String> getKeys() {
        String pattern = Optional.ofNullable(TYPE_PATTERNS.get(targetType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + targetType));
        return scanKeys(pattern);
    }

    private List<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> keys = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error scanning Redis keys with pattern: " + pattern, e);
            }
            return keys;
        });
    }
}
