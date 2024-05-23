package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@RequiredArgsConstructor
public class ViewCountPagingItemReader extends AbstractPagingItemReader<ViewCount> {
    private final String BASE_KEY_NAME;
    private final RedisTemplate<String, ViewCount> redisTemplate;

    @Override
    protected void doReadPage() {
        if(results ==null){
            results = new ArrayList<>();
        }
        else{
            results.clear();
        }
        List<LessonViewCount> lessonViewCounts = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<LessonViewCount> execute(RedisOperations operations) throws DataAccessException {
                List<String> keys = scanKeys(BASE_KEY_NAME+":*");
                List<LessonViewCount> items = new ArrayList<>();
                if(keys!=null){
                    int start =  (getPage() * getPageSize());
                    int end = (keys.size() < (getPage() + 1) * getPageSize() ? keys.size() : (getPage() + 1) * getPageSize());

                    List<String> pagingKeys = new ArrayList<>(keys).subList(start,
                            end);
                    for(String key : pagingKeys){
                        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                        items.add(new LessonViewCount(
                                Long.parseLong((String) entries.get("id")),
                                Integer.parseInt((String) entries.get("viewCount"))));
                    }
                }
                return new ArrayList<>(items);
            }

        });
        results.addAll(lessonViewCounts);
    }

    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan( ScanOptions.scanOptions().match(pattern).build());
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        Collections.sort(keys); // 순서를 보장하기 위해 정렬
        return keys;
    }
}
