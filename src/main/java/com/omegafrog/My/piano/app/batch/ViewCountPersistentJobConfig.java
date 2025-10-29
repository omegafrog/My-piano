package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.omegafrog.My.piano.app.web.domain.post.PostLikeCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.querydsl.jpa.impl.JPAQueryFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final static int BATCH_SIZE = 100;

    @Autowired
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    private final SheetPostIndexRepository sheetPostIndexRepository;
    @Autowired
    private final SheetPostRepository sheetPostRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Bean("persistViewCountJob")
    public Job persistViewCountJob(JobRepository jobRepository) throws Exception {
        return new JobBuilder("PersistViewCountJob", jobRepository)
                .start(persistLessonViewCountStep(jobRepository))
                .next(persistentPostViewCountStep(jobRepository))
                .next(persistentSheetPostViewCountStep(jobRepository))
                .next(persistentPostLikeCountStep(jobRepository))
                .next(persistentSheetPostLikeCountStep(jobRepository))
                .next(persistentLessonLikeCountStep(jobRepository))
                .next(updateRankingStep(jobRepository))
                .next(removeRankingStep(jobRepository))
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
        ViewCountPagingItemReader itemReader = new ViewCountPagingItemReader(PostViewCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
    }

    @Bean
    public ViewCountPagingItemReader lessonViewCountPagingItemReader() {
        ViewCountPagingItemReader itemReader = new ViewCountPagingItemReader(LessonViewCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
    }

    @Bean
    public ViewCountPagingItemReader sheetPostViewCountPagingItemReader() {
        ViewCountPagingItemReader itemReader = new ViewCountPagingItemReader(SheetPostViewCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
    }

    @Bean
    public LikeCountPagingItemReader postLikeCountPagingItemReader() {
        LikeCountPagingItemReader itemReader = new LikeCountPagingItemReader(PostLikeCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
    }

    @Bean
    public LikeCountPagingItemReader lessonLikeCountPagingItemReader() {
        LikeCountPagingItemReader itemReader = new LikeCountPagingItemReader(LessonLikeCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
    }

    @Bean
    public LikeCountPagingItemReader sheetPostLikeCountPagingItemReader() {
        LikeCountPagingItemReader itemReader = new LikeCountPagingItemReader(SheetPostLikeCount.class, redisTemplate);
        itemReader.setPageSize(BATCH_SIZE);
        return itemReader;
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

    @Bean
    public Step removeRankingStep(JobRepository jobRepository) {
        return new StepBuilder("RemoveRankingStep", jobRepository)
                .<SheetPostIndex, SheetPostIndex>chunk(100, transactionManager)
                .reader(deleteJpaPagingItemReader())
                .writer(deleteItemWriter())
                .build();
    }

    @Bean
    public ElasticsearchItemReader<SheetPostIndex> deleteJpaPagingItemReader() {
        return new ElasticsearchItemReader<>(
                100,
                elasticsearchClient);
    }

    @Bean
    public ItemWriter<SheetPostIndex> deleteItemWriter() {
        return new ItemWriter<SheetPostIndex>() {
            @Override
            public void write(Chunk<? extends SheetPostIndex> chunk) throws Exception {
                log.info("{}", chunk.getItems().get(0).getId());
                Iterable<SheetPost> allById = sheetPostRepository.findAllById(chunk.getItems()
                        .stream().map(SheetPostIndex::getId).toList());
                List<Long> allIds = ((List<SheetPost>) allById).stream().map(item -> item.getId())
                        .toList();

                List<Long> a = chunk.getItems().stream().map(item -> item.getId())
                        .collect(Collectors.toList());
                List<Long> list = a.stream().filter(
                        id -> !allIds.contains(id)).toList();
                // for (Long id : a) {
                // for (Long id2 : allIds) {
                // if (id.equals(id2)) {
                // a.remove(id);
                // }
                // }
                // }
                sheetPostIndexRepository.deleteByIdIn(list);
            }
        };

    }

    @Bean
    public Step updateRankingStep(JobRepository jobRepository) throws Exception {
        return new StepBuilder("UpdateRankingStep", jobRepository)
                .<SheetPost, SheetPostIndex>chunk(100, transactionManager)
                .reader(jpaPagingItemReader())
                .processor(invertToSheetPostIndex())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<SheetPost> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<SheetPost>()
                .entityManagerFactory(entityManagerFactory)
                .queryString("from SheetPost p JOIN FETCH p.sheet JOIN FETCH p.author ORDER by p.id ASC")
                .pageSize(100)
                .name("jpaPagingItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<SheetPost, SheetPostIndex> invertToSheetPostIndex() {
        return new ItemProcessor<SheetPost, SheetPostIndex>() {
            @Override
            public SheetPostIndex process(SheetPost item) throws Exception {
                // log.info("item:{}", item.toString());
                return SheetPostIndex.of(item);
            }
        };
    }

    @Bean
    public ItemWriter<SheetPostIndex> itemWriter() {
        return new ItemWriter<SheetPostIndex>() {
            @Override
            public void write(Chunk<? extends SheetPostIndex> chunk) throws Exception {
                List<? extends SheetPostIndex> items = chunk.getItems();
                // for (SheetPostIndex item : items) {
                // log.info("created_at:{}", item.getCreated_at());
                // sheetPostIndexRepository.save(item);
                // }
                sheetPostIndexRepository.saveAll(items);
            }
        };
    }
}
