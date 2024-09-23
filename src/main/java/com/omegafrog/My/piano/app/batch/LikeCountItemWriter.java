package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LikeCountItemWriter<T extends Article> implements ItemWriter<LikeCount> {
    private final JPAQueryFactory factory;
    private final EntityPathBase<T> entityPath;

    public LikeCountItemWriter(EntityPathBase<T> entityPathBase, JPAQueryFactory factory) {
        this.entityPath = entityPathBase;
        this.factory = factory;
    }

    @Override
    public void write(Chunk<? extends LikeCount> chunk) throws Exception {

        Map<Long, Integer> changedLikeCountMap = chunk.getItems().stream()
                .collect(Collectors.toMap(LikeCount::getId, LikeCount::getLikeCount));
        List<T> fetched = factory.selectFrom(entityPath)
                .where(GenericEntityPathUtil.getLongProperty(entityPath, "id").in(changedLikeCountMap.keySet()))
                .fetch();
        fetched.forEach(toUpdate -> toUpdate.setLikeCount(changedLikeCountMap.get(toUpdate.getId())));

    }
}
