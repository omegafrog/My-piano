package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LikeCountItemWriter<T extends Article> implements ItemWriter<LikeCount> {
    private final JPAQueryFactory factory;
    private final EntityPathBase<T> entityPath;

    public LikeCountItemWriter(EntityPathBase<T> entityPathBase, JPAQueryFactory factory) {
        this.entityPath = entityPathBase;
        this.factory = factory;
    }

    @Override
    @Transactional
    public void write(Chunk<? extends LikeCount> chunk) throws Exception {
        if (chunk.isEmpty()) {
            log.info("LikeCountItemWriter: No items to process");
            return;
        }
        
        long startTime = System.currentTimeMillis();
        Map<Long, Integer> likeCountMap = chunk.getItems().stream()
                .collect(Collectors.toMap(LikeCount::getId, LikeCount::getLikeCount));
        NumberPath<Long> idPath = Expressions.numberPath(Long.class, entityPath, "id");
        NumberPath<Integer> likeCountPath = Expressions.numberPath(Integer.class, entityPath, "likeCount");

        int updatedRows = 0;
        for (Map.Entry<Long, Integer> entry : likeCountMap.entrySet()) {
            updatedRows += factory
                    .update(entityPath)
                    .set(likeCountPath, entry.getValue())
                    .where(idPath.eq(entry.getKey()))
                    .execute();
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        log.info("LikeCountItemWriter Metrics - Entity: {}, Input: {} items, Updated: {} rows, Time: {}ms",
                entityPath.getType().getSimpleName(), chunk.size(), updatedRows, processingTime);

        if (updatedRows != chunk.size()) {
            log.warn("LikeCountItemWriter Warning - Expected {} updates but {} rows affected for entity: {}",
                    chunk.size(), updatedRows, entityPath.getType().getSimpleName());
        }
    }
}
