package com.omegafrog.My.piano.app.batch;


import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.*;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ViewCountPersistentJobConfig {

    private final JPAQueryFactory factory;
    private final PlatformTransactionManager transactionManager;
    private final RedisTemplate redisTemplate;
    private final static String LESSON_KEY_NAME="lesson";
    private final static String POST_KEY_NAME="post";
    private final static String SHEET_POST_KEY_NAME="sheetpost";
    @Bean
    @Qualifier("persistViewCountJob")
    public Job persistViewCountJob(JobRepository jobRepository){
        return new JobBuilder("PersistViewCountJob", jobRepository)
                .start(persistLessonViewCountStep(jobRepository))
                .next(persistentPostViewCountStep(jobRepository))
                .next(persistentSheetPostViewCountStep(jobRepository))
                .build();
    }
    @Bean
    public Step persistLessonViewCountStep(JobRepository jobRepository){
        return new StepBuilder("persistViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100,transactionManager)
                .reader(lessonViewCountPagingItemReader())
                .writer(lessonViewCountWriter())
                .build();
    }
    @Bean
    public Step persistentPostViewCountStep(JobRepository jobRepository){
        return new StepBuilder("persistentPostViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100, transactionManager)
                .reader(postViewCountPagingItemReader())
                .writer(postViewCountWriter())
                .build();
    }

    @Bean
    public Step persistentSheetPostViewCountStep(JobRepository jobRepository){
        return new StepBuilder("persistentSheetPostViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100, transactionManager)
                .reader(sheetPostViewCountPagingItemReader())
                .writer(sheetPostViewCountWriter())
                .build();
    }
    @Bean
    public ViewCountPagingItemReader postViewCountPagingItemReader(){
        return new ViewCountPagingItemReader(POST_KEY_NAME,redisTemplate) ;
    }
    @Bean
    public ViewCountPagingItemReader lessonViewCountPagingItemReader(){
        return new ViewCountPagingItemReader(LESSON_KEY_NAME,redisTemplate) ;
    }
    @Bean
    public ViewCountPagingItemReader sheetPostViewCountPagingItemReader(){
        return new ViewCountPagingItemReader(SHEET_POST_KEY_NAME,redisTemplate) ;
    }
    @Bean
    public ItemWriter<ViewCount> lessonViewCountWriter(){
        return new ViewCountItemWriter<>(QLesson.lesson, factory);
    }

    @Bean
    public ItemWriter<ViewCount> postViewCountWriter(){
        return new ViewCountItemWriter<>(QPost.post, factory);
    }
    @Bean
    public ItemWriter<ViewCount> sheetPostViewCountWriter(){
        return new ViewCountItemWriter<>(QSheetPost.sheetPost, factory);
    }
}
