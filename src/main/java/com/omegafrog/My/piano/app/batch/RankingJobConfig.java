package com.omegafrog.My.piano.app.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class RankingJobConfig {

    @Autowired
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final SheetPostIndexRepository sheetPostIndexRepository;
    @Autowired
    private final SheetPostRepository sheetPostRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Bean("updateRankingJob")
    public Job UpdateRankingJob(JobRepository jobRepository) throws Exception {
        return new JobBuilder("UpdateRankingJob", jobRepository)
                .start(updateRankingStep(jobRepository))
                .next(removeRankingStep(jobRepository))
                .build();
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
