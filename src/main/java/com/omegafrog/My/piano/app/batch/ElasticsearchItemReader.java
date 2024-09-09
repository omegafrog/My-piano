package com.omegafrog.My.piano.app.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.Iterator;

import static io.jsonwebtoken.lang.Assert.state;
import static org.springframework.util.ClassUtils.getShortName;

@Slf4j
public class ElasticsearchItemReader<T> extends AbstractPaginatedDataItemReader<T> implements InitializingBean {


    private final ElasticsearchOperations elasticsearchOperations;

    private final Query query;

    private final Class<? extends T> targetType;

    public ElasticsearchItemReader(ElasticsearchOperations elasticsearchOperations, Query query, Class<? extends T> targetType) {
        setName(getShortName(getClass()));
        this.elasticsearchOperations = elasticsearchOperations;
        this.query = query;
        this.targetType = targetType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        state(elasticsearchOperations != null, "An ElasticsearchOperations implementation is required.");
        state(query != null, "A query is required.");
        state(targetType != null, "A target type to convert the input into is required.");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Iterator<T> doPageRead() {

        log.debug("executing query {}", query);

        return (Iterator<T>) elasticsearchOperations.search(query, targetType)
                .stream().map(SearchHit::getContent).iterator();
    }
}
