package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ViewCountItemWriter<T extends Article> implements ItemWriter<ViewCount> {
    private final EntityPathBase<T> entityPath;
    private final JPAQueryFactory factory;
    
    @PersistenceContext
    private EntityManager entityManager;

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
        
        String tableName = getTableName();
        
        // 진짜 bulk update: 단일 CASE WHEN 쿼리
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName)
           .append(" SET view_count = view_count + CASE ");
        
        for (Long id : viewCountMap.keySet()) {
            sql.append("WHEN id = ").append(id).append(" THEN ").append(viewCountMap.get(id)).append(" ");
        }
        
        sql.append("END WHERE id IN (");
        sql.append(viewCountMap.keySet().stream()
                  .map(String::valueOf)
                  .collect(Collectors.joining(", ")));
        sql.append(")");
        
        int updatedRows = entityManager.createNativeQuery(sql.toString()).executeUpdate();
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        log.info("ViewCountItemWriter Metrics - Table: {}, Input: {} items, Updated: {} rows, Time: {}ms", 
                tableName, chunk.size(), updatedRows, processingTime);
        
        if (updatedRows != chunk.size()) {
            log.warn("ViewCountItemWriter Warning - Expected {} updates but {} rows affected for table: {}", 
                    chunk.size(), updatedRows, tableName);
        }
    }
    
    private String getTableName() {
        String entityName = entityPath.getType().getSimpleName();
        // Entity 이름을 테이블 이름으로 변환 (camelCase -> snake_case)
        return entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase() + "s";
    }
}