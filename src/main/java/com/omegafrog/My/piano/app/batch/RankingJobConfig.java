package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
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
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RankingJobConfig {

    @Autowired
    private final DataSource dataSource;

    @Autowired
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final SheetPostIndexRepository sheetPostIndexRepository;


    @Bean
    public Job UpdateRankingJob(JobRepository jobRepository) throws Exception {
        return new JobBuilder("UpdateRankingJob", jobRepository)
                .start(updateRankingStep(jobRepository))
                .build();
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
    public JpaPagingItemReader<SheetPost> jpaPagingItemReader()  {
        return new JpaPagingItemReaderBuilder<SheetPost>()
                .entityManagerFactory(entityManagerFactory)
                .queryString("from SheetPost")
                .pageSize(100)
                .name("jpaPagingItemReader")
                .build();
    }


    @Bean
    public ItemProcessor<SheetPost, SheetPostIndex> invertToSheetPostIndex() {
        return new ItemProcessor<SheetPost, SheetPostIndex>() {
            @Override
            public SheetPostIndex process(SheetPost item) throws Exception {
                log.info("item:{}", item.toString());
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
                for (SheetPostIndex item : items) {
                    log.info("created_at:{}", item.getCreated_at());
                    sheetPostIndexRepository.save(item);
                }
            }
        };
    }
}
