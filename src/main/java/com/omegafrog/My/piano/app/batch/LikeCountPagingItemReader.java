package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LikeCountPagingItemReader<T extends LikeCount> extends AbstractPagingItemReader<LikeCount> {

    private final Class<T> targetType;
    private final RedisTemplate<String, ViewCount> redisTemplate;
    private List<String> allKeys;
    private int totalProcessed = 0;
    private int totalReset = 0;

    public LikeCountPagingItemReader(Class<T> targetType, RedisTemplate<String, ViewCount> redisTemplate) {
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
        
        log.info("LikeCountPagingItemReader opened - Type: {}, Total keys found: {}, Scan time: {}ms", 
                targetType.getSimpleName(), allKeys.size(), endTime - startTime);
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        log.info("LikeCountPagingItemReader closed - Type: {}, Total processed: {}, Total reset: {}", 
                targetType.getSimpleName(), totalProcessed, totalReset);
        this.allKeys.clear();
        this.totalProcessed = 0;
        this.totalReset = 0;
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
        
        List<LikeCount> likeCounts = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<LikeCount> execute(RedisOperations operations) throws DataAccessException {
                List<LikeCount> items = new ArrayList<>();
                Constructor<T> constructor = getConstructor();
                
                for (String key : pageKeys) {
                    try {
                        Object likeCountValue = operations.opsForHash().get(key, "likeCount");
                        if (likeCountValue != null) {
                            int currentLikeCount = Integer.parseInt(likeCountValue.toString());
                            if (currentLikeCount > 0) {
                                items.add(constructor.newInstance(
                                        Long.valueOf(key.split(":")[1]),
                                        currentLikeCount
                                ));
                                // 읽은 후 0으로 초기화
                                operations.opsForHash().put(key, "likeCount", "0");
                                totalProcessed++;
                                totalReset++;
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing key: " + key, e);
                    }
                }
                return items;
            }
        });
        
        results.addAll(likeCounts);
    }

    private @NotNull Constructor<T> getConstructor() {
        Constructor<T> constructor = null;
        try {
            constructor = targetType.getConstructor(Long.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return constructor;
    }

    private @NotNull List<String> getKeys() {
        try {
            String keyName = (String) targetType.getField("KEY_NAME").get(null);
            return scanKeys(keyName + ":*");
        } catch (Exception e) {
            throw new RuntimeException("Error getting key pattern for type: " + targetType, e);
        }
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
