package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LikeCountPagingItemReader<T extends LikeCount> extends AbstractPagingItemReader<LikeCount> {

    private final Class<T> targetType;
    private final RedisTemplate<String, ViewCount> redisTemplate;

    public LikeCountPagingItemReader(Class<T> targetType, RedisTemplate<String, ViewCount> redisTemplate) {
        this.targetType = targetType;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new ArrayList<>();
        } else {
            results.clear();
        }
        List<LikeCount> viewCounts = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<LikeCount> execute(RedisOperations operations) throws DataAccessException {
                List<String> keys = getKeys();
                List<LikeCount> items = new ArrayList<>();
                Constructor<T> constructor = getConstructor();

                int start = (getPage() * getPageSize());
                int end = (keys.size() < (getPage() + 1) * getPageSize() ? keys.size() : (getPage() + 1) * getPageSize());

                List<String> pagingKeys = new ArrayList<>(keys).subList(start,
                        end);
                for (String key : pagingKeys) {
                    try {
                        items.add(constructor.newInstance(
                                Long.valueOf(key.split(":")[1]),
                                (int) operations.opsForHash().get(key, "likeCount")
                        ));
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                return new ArrayList<>(items);
            }

        });
        results.addAll(viewCounts);
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
        List<String> keys = null;
        try {
            keys = scanKeys(
                    targetType.getField("KEY_NAME") + ":*");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(ScanOptions.scanOptions().
                match(pattern).build());
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        Collections.sort(keys); // 순서를 보장하기 위해 정렬
        return keys;
    }
}
