package com.omegafrog.My.piano.app.batch;


import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ViewCountPersistentJobConfig {

    private final JPAQueryFactory factory;
    private final PlatformTransactionManager transactionManager;
    private final RedisTemplate redisTemplate;
    @Bean
    @Qualifier("persistViewCountJob")
    public Job persistViewCountJob(JobRepository jobRepository){
        return new JobBuilder("PersistViewCountJob", jobRepository)
                .start(persistViewCountStep(jobRepository))
                .build();
    }
    @Bean
    public Step persistViewCountStep(JobRepository jobRepository){
        return new StepBuilder("persistViewCountStep", jobRepository)
                .<LessonViewCount, LessonViewCount>chunk(100,transactionManager)
                .reader(lessonViewCountPagingItemReader())
                .writer(updateLesson())
                .build();
    }

    @Bean
    public LessonViewCountPagingItemReader lessonViewCountPagingItemReader(){
        return new LessonViewCountPagingItemReader(redisTemplate);
    }

    @RequiredArgsConstructor
    public class LessonViewCountPagingItemReader extends AbstractPagingItemReader<LessonViewCount> {
        private final RedisTemplate<String, LessonViewCount> redisTemplate;

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
                    List<String> keys = scanKeys("lesson:*");
                    List<LessonViewCount> items = new ArrayList<>();
                    if(keys!=null){
                        int start =  (getPage() * getPageSize());
                        int end = (keys.size() < (getPage() + 1) * getPageSize() ? keys.size() : (getPage() + 1) * getPageSize());

                        List<String> pagingKeys = new ArrayList<>(keys).subList(start,
                                end);
                        for(String key : pagingKeys){
                            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                            items.add(new LessonViewCount(Long.parseLong((String) entries.get("lessonId")), Integer.parseInt((String) entries.get("viewCount"))));
                        }
                    }
                    return new ArrayList<>(items);
                }

            });
            results.addAll(lessonViewCounts);
        }
    }
    public List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan( ScanOptions.scanOptions().match(pattern).build());
        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        Collections.sort(keys); // 순서를 보장하기 위해 정렬
        return keys;
    }

    public class LessonViewCountItemWriter implements ItemWriter<LessonViewCount> {
        @Override
        public void write(Chunk<? extends LessonViewCount> chunk) throws Exception {
            log.debug("chunk:{}", chunk);
            QLesson lesson = QLesson.lesson;
            Map<Long, Integer> changedLessonViewCount = new HashMap<>();
            BooleanExpression findExpression = null;
            for (LessonViewCount lessonViewCount : chunk) {
                changedLessonViewCount.put(lessonViewCount.getLessonId(), lessonViewCount.getViewCount());
                BooleanExpression newExpression = lesson.id.eq(lessonViewCount.getLessonId());
                findExpression = (findExpression == null)? newExpression : findExpression.or(newExpression);
            }
            if(findExpression == null) return;
            List<Lesson> fetched = factory.selectFrom(lesson)
                    .where(findExpression)
                    .fetch();
            log.debug("fetched:{}", fetched);
            fetched.forEach(toUpdate -> toUpdate.setViewCount(changedLessonViewCount.get(toUpdate.getId())));
        }
    }
    @Bean
    public ItemWriter<LessonViewCount> updateLesson(){
        return new LessonViewCountItemWriter();
    }

}
