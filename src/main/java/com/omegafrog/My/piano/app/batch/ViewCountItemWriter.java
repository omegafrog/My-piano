package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ViewCountItemWriter<T extends Article> implements ItemWriter<ViewCount> {
    private final EntityPathBase<T> entityPath;
    private final JPAQueryFactory factory;
    

    @Override
    @Transactional
    public void write(Chunk<? extends ViewCount> chunk) throws Exception {
        if (chunk.isEmpty()) {
            log.info("ViewCountItemWriter: No items to process");
            return;
        }
        
        long startTime = System.currentTimeMillis();
        Map<Long, Integer> viewCountMap = chunk.getItems().stream()
                .collect(Collectors.toMap(ViewCount::getId, ViewCount::getViewCount));
        
        // QueryDSL을 사용한 개별 업데이트
        // Reflection을 사용해서 필드에 접근
        NumberPath<Long> idPath;
        NumberPath<Integer> viewCountPath;
        
        try {
            idPath = (NumberPath<Long>) entityPath.getClass().getField("id").get(entityPath);
            viewCountPath = (NumberPath<Integer>) entityPath.getClass().getField("viewCount").get(entityPath);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access entity fields", e);
        }
        
        // 각 항목을 개별적으로 업데이트
        long updatedRows = 0;
        for (Map.Entry<Long, Integer> entry : viewCountMap.entrySet()) {
            long result = factory.update(entityPath)
                    .set(viewCountPath, entry.getValue())
                    .where(idPath.eq(entry.getKey()))
                    .execute();
            updatedRows += result;
        }
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        String entityName = entityPath.getType().getSimpleName();
        log.info("ViewCountItemWriter Metrics - Entity: {}, Input: {} items, Updated: {} rows, Time: {}ms", 
                entityName, chunk.size(), updatedRows, processingTime);
        
        if (updatedRows != chunk.size()) {
            log.warn("ViewCountItemWriter Warning - Expected {} updates but {} rows affected for entity: {}", 
                    chunk.size(), updatedRows, entityName);
        }
    }
}