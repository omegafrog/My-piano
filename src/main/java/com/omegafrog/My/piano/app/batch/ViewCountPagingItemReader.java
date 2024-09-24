package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ViewCountPagingItemReader<ViewCount> extends AbstractPagingItemReader<ViewCount> {
    private final Class<ViewCount> targetType;
    private final RedisTemplate<String, ViewCount> redisTemplate;

    public ViewCountPagingItemReader(Class<ViewCount> targetType, RedisTemplate<String, ViewCount> redisTemplate) {
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
        List<ViewCount> viewCounts = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<ViewCount> execute(RedisOperations operations) throws DataAccessException {
                List<String> keys = getKeys();
                List<ViewCount> items = new ArrayList<>();
                Constructor<ViewCount> constructor = getConstructor();

                int start = (getPage() * getPageSize());
                int end = (Math.min(keys.size(), (getPage() + 1) * getPageSize()));

                List<String> pagingKeys = new ArrayList<>(keys).subList(start,
                        end);
                for (String key : pagingKeys) {
                    try {
                        items.add(constructor.newInstance(
                                Long.valueOf(key.split(":")[1]),
                                Integer.parseInt((String) operations.opsForHash().get(key, "viewCount"))
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
