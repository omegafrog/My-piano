package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ViewCountItemWriter<T extends Article> implements ItemWriter<ViewCount> {
    private final EntityPathBase<T> entityPath;
    private final JPAQueryFactory factory;

    @Override
    public void write(Chunk<? extends ViewCount> chunk) throws Exception {
        Map<Long, Integer> changedLessonViewCountMap = chunk.getItems().stream()
                .collect(Collectors.toMap(ViewCount::getId, ViewCount::getViewCount));
        List<T> fetched = factory.selectFrom(entityPath)
                .where(GenericEntityPathUtil.getLongProperty(entityPath, "id").in(changedLessonViewCountMap.keySet()))
                .fetch();
        fetched.forEach(toUpdate -> toUpdate.setViewCount(changedLessonViewCountMap.get(toUpdate.getId())));
    }
}
