package com.omegafrog.My.piano.app.batch;


import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.omegafrog.My.piano.app.web.domain.post.PostLikeCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
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
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ViewCountPersistentJobConfig {

    private final JPAQueryFactory factory;
    private final PlatformTransactionManager transactionManager;
    private final RedisTemplate redisTemplate;
    private final static String LESSON_KEY_NAME = "lesson";
    private final static String POST_KEY_NAME = "post";
    private final static String SHEET_POST_KEY_NAME = "sheetpost";

    @Bean
    @Qualifier("persistCountJob")
    public Job persistViewCountJob(JobRepository jobRepository) {
        return new JobBuilder("PersistViewCountJob", jobRepository)
                .start(persistLessonViewCountStep(jobRepository))
                .next(persistentPostViewCountStep(jobRepository))
                .next(persistentSheetPostViewCountStep(jobRepository))
                .next(persistentPostLikeCountStep(jobRepository))
                .next(persistentSheetPostLikeCountStep(jobRepository))
                .next(persistentLessonLikeCountStep(jobRepository))
                .build();
    }

    @Bean
    public Step persistLessonViewCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100, transactionManager)
                .reader(lessonViewCountPagingItemReader())
                .writer(lessonViewCountWriter())
                .build();
    }

    @Bean
    public Step persistentPostViewCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistentPostViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100, transactionManager)
                .reader(postViewCountPagingItemReader())
                .writer(postViewCountWriter())
                .build();
    }

    @Bean
    public Step persistentSheetPostViewCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistentSheetPostViewCountStep", jobRepository)
                .<ViewCount, ViewCount>chunk(100, transactionManager)
                .reader(sheetPostViewCountPagingItemReader())
                .writer(sheetPostViewCountWriter())
                .build();
    }

    @Bean
    public Step persistentSheetPostLikeCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistentSheetPostLikeCountStep", jobRepository)
                .<LikeCount, LikeCount>chunk(100, transactionManager)
                .reader(sheetPostLikeCountPagingItemReader())
                .writer(sheetPostLikeCountWriter())
                .build();
    }

    @Bean
    public Step persistentPostLikeCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistentPostLikeCountStep", jobRepository)
                .<LikeCount, LikeCount>chunk(100, transactionManager)
                .reader(postLikeCountPagingItemReader())
                .writer(postLikeCountWriter())
                .build();
    }

    @Bean
    public Step persistentLessonLikeCountStep(JobRepository jobRepository) {
        return new StepBuilder("persistentLessonLikeCountStep", jobRepository)
                .<LikeCount, LikeCount>chunk(100, transactionManager)
                .reader(lessonLikeCountPagingItemReader())
                .writer(lessonLikeCountWriter())
                .build();
    }


    @Bean
    public ViewCountPagingItemReader postViewCountPagingItemReader() {
        return new ViewCountPagingItemReader(PostViewCount.class, redisTemplate);
    }

    @Bean
    public ViewCountPagingItemReader lessonViewCountPagingItemReader() {
        return new ViewCountPagingItemReader(LessonViewCount.class, redisTemplate);
    }

    @Bean
    public ViewCountPagingItemReader sheetPostViewCountPagingItemReader() {
        return new ViewCountPagingItemReader(SheetPostViewCount.class, redisTemplate);
    }

    @Bean
    public LikeCountPagingItemReader postLikeCountPagingItemReader() {
        return new LikeCountPagingItemReader(PostLikeCount.class, redisTemplate);
    }

    @Bean
    public LikeCountPagingItemReader lessonLikeCountPagingItemReader() {
        return new LikeCountPagingItemReader(LessonLikeCount.class, redisTemplate);
    }

    @Bean
    public LikeCountPagingItemReader sheetPostLikeCountPagingItemReader() {
        return new LikeCountPagingItemReader(SheetPostLikeCount.class, redisTemplate);
    }

    @Bean
    public ItemWriter<ViewCount> lessonViewCountWriter() {
        return new ViewCountItemWriter<>(QLesson.lesson, factory);
    }

    @Bean
    public ItemWriter<ViewCount> postViewCountWriter() {
        return new ViewCountItemWriter<>(QPost.post, factory);
    }

    @Bean
    public ItemWriter<ViewCount> sheetPostViewCountWriter() {
        return new ViewCountItemWriter<>(QSheetPost.sheetPost, factory);
    }

    @Bean
    public ItemWriter<LikeCount> lessonLikeCountWriter() {
        return new LikeCountItemWriter<>(QLesson.lesson, factory);
    }

    @Bean
    public ItemWriter<LikeCount> postLikeCountWriter() {
        return new LikeCountItemWriter<>(QPost.post, factory);
    }

    @Bean
    public ItemWriter<LikeCount> sheetPostLikeCountWriter() {
        return new LikeCountItemWriter<>(QSheetPost.sheetPost, factory);
    }
}
