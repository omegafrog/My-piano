package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LikeCountItemWriter<T extends Article> implements ItemWriter<LikeCount> {
    private final JPAQueryFactory factory;
    private final EntityPathBase<T> entityPath;
    
    @PersistenceContext
    private EntityManager entityManager;

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
        
        String tableName = getTableName();
        
        // 진짜 bulk update: 단일 CASE WHEN 쿼리
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName)
           .append(" SET like_count = like_count + CASE ");
        
        for (Long id : likeCountMap.keySet()) {
            sql.append("WHEN id = ").append(id).append(" THEN ").append(likeCountMap.get(id)).append(" ");
        }
        
        sql.append("END WHERE id IN (");
        sql.append(likeCountMap.keySet().stream()
                  .map(String::valueOf)
                  .collect(Collectors.joining(", ")));
        sql.append(")");
        
        int updatedRows = entityManager.createNativeQuery(sql.toString()).executeUpdate();
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        log.info("LikeCountItemWriter Metrics - Table: {}, Input: {} items, Updated: {} rows, Time: {}ms", 
                tableName, chunk.size(), updatedRows, processingTime);
        
        if (updatedRows != chunk.size()) {
            log.warn("LikeCountItemWriter Warning - Expected {} updates but {} rows affected for table: {}", 
                    chunk.size(), updatedRows, tableName);
        }
    }
    
    private String getTableName() {
        String entityName = entityPath.getType().getSimpleName();
        // Entity 이름을 테이블 이름으로 변환 (camelCase -> snake_case)
        return entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase() + "s";
    }
}