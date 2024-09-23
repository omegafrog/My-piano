package com.omegafrog.My.piano.app.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.springframework.util.ClassUtils.getShortName;

@Slf4j
public class ElasticsearchItemReader<T> extends AbstractPaginatedDataItemReader<T> implements InitializingBean {
    private String scrollId;
    private int pageSize = 100;

    @Autowired
    private ObjectMapper objectMapper;
    private final ElasticsearchClient client;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public ElasticsearchItemReader(int pageSize, ElasticsearchClient client) {
        this.client = client;
        setName(getShortName(getClass()));
        this.pageSize = pageSize;
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    protected Iterator<T> doPageRead() {
        List<SheetPostIndex> result = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("sheetpost")
                .query(Query.of(q -> q.matchAll(t -> t)))
                .size(pageSize)
                .scroll(Time.of(t -> t.time("1m")))
                .sort(SortOptions.of(so -> so.field(f -> f.field("id").order(SortOrder.Asc))))
                .build();

        if (scrollId == null) {
            SearchResponse<SheetPostIndex> search = client.search(searchRequest, SheetPostIndex.class);
            scrollId = search.scrollId();
            search.hits().hits().forEach(hit -> result.add(
                    hit.source()));
        } else {
            ScrollRequest scrollRequest = new ScrollRequest.Builder()
                    .scrollId(scrollId)
                    .scroll(Time.of(t -> t.time("1m")))
                    .build();
            ScrollResponse<SheetPostIndex> scrollResponse = client.scroll(scrollRequest, SheetPostIndex.class);
            scrollId = scrollResponse.scrollId();
            scrollResponse.hits().hits().forEach(hit -> result.add(
                    hit.source()));
        }
        return (Iterator<T>) result.iterator();
    }
}
