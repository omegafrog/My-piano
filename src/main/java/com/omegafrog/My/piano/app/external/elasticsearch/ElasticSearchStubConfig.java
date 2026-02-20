package com.omegafrog.My.piano.app.external.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Configuration
@Profile("test")
public class ElasticSearchStubConfig {

    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient() {
        return new ElasticsearchClient((ElasticsearchTransport) null);
    }

    @Bean
    @Primary
    public SheetPostIndexRepository sheetPostIndexRepository() {
        Map<Long, SheetPostIndex> store = new ConcurrentHashMap<>();

        return new SheetPostIndexRepository() {
            @Override
            public long deleteByIdIn(Collection<Long> ids) {
                long deleted = 0;
                for (Long id : ids) {
                    if (store.remove(id) != null) {
                        deleted++;
                    }
                }
                return deleted;
            }

            @Override
            public Slice<SheetPostIndex> findAll(Pageable pageable) {
                List<SheetPostIndex> values = new ArrayList<>(store.values());
                int from = (int) pageable.getOffset();
                if (from >= values.size()) {
                    return new SliceImpl<>(List.of(), pageable, false);
                }
                int to = Math.min(from + pageable.getPageSize(), values.size());
                boolean hasNext = to < values.size();
                return new SliceImpl<>(values.subList(from, to), pageable, hasNext);
            }

            @Override
            public <S extends SheetPostIndex> S save(S entity) {
                if (entity != null && entity.getId() != null) {
                    store.put(entity.getId(), entity);
                }
                return entity;
            }

            @Override
            public <S extends SheetPostIndex> Iterable<S> saveAll(Iterable<S> entities) {
                for (S entity : entities) {
                    save(entity);
                }
                return entities;
            }

            @Override
            public Optional<SheetPostIndex> findById(Long id) {
                return Optional.ofNullable(store.get(id));
            }

            @Override
            public boolean existsById(Long id) {
                return store.containsKey(id);
            }

            @Override
            public Iterable<SheetPostIndex> findAll() {
                return store.values();
            }

            @Override
            public Iterable<SheetPostIndex> findAllById(Iterable<Long> ids) {
                List<SheetPostIndex> result = new ArrayList<>();
                for (Long id : ids) {
                    SheetPostIndex found = store.get(id);
                    if (found != null) {
                        result.add(found);
                    }
                }
                return result;
            }

            @Override
            public long count() {
                return store.size();
            }

            @Override
            public void deleteById(Long id) {
                store.remove(id);
            }

            @Override
            public void delete(SheetPostIndex entity) {
                if (entity != null && entity.getId() != null) {
                    store.remove(entity.getId());
                }
            }

            @Override
            public void deleteAllById(Iterable<? extends Long> ids) {
                for (Long id : ids) {
                    store.remove(id);
                }
            }

            @Override
            public void deleteAll(Iterable<? extends SheetPostIndex> entities) {
                for (SheetPostIndex entity : entities) {
                    delete(entity);
                }
            }

            @Override
            public void deleteAll() {
                store.clear();
            }
        };
    }

    @Bean
    @Primary
    public SheetPostSearchIndexRepository sheetPostSearchIndexRepository() {
        Map<String, SheetPostSearchIndex> store = new ConcurrentHashMap<>();

        return new SheetPostSearchIndexRepository() {
            @Override
            public <S extends SheetPostSearchIndex> S save(S entity) {
                if (entity != null && entity.getId() != null) {
                    store.put(entity.getId(), entity);
                }
                return entity;
            }

            @Override
            public <S extends SheetPostSearchIndex> Iterable<S> saveAll(Iterable<S> entities) {
                for (S entity : entities) {
                    save(entity);
                }
                return entities;
            }

            @Override
            public Optional<SheetPostSearchIndex> findById(String id) {
                return Optional.ofNullable(store.get(id));
            }

            @Override
            public boolean existsById(String id) {
                return store.containsKey(id);
            }

            @Override
            public Iterable<SheetPostSearchIndex> findAll() {
                return store.values();
            }

            @Override
            public Iterable<SheetPostSearchIndex> findAllById(Iterable<String> ids) {
                List<SheetPostSearchIndex> result = new ArrayList<>();
                for (String id : ids) {
                    SheetPostSearchIndex found = store.get(id);
                    if (found != null) {
                        result.add(found);
                    }
                }
                return result;
            }

            @Override
            public long count() {
                return store.size();
            }

            @Override
            public void deleteById(String id) {
                store.remove(id);
            }

            @Override
            public void delete(SheetPostSearchIndex entity) {
                if (entity != null && entity.getId() != null) {
                    store.remove(entity.getId());
                }
            }

            @Override
            public void deleteAllById(Iterable<? extends String> ids) {
                for (String id : ids) {
                    store.remove(id);
                }
            }

            @Override
            public void deleteAll(Iterable<? extends SheetPostSearchIndex> entities) {
                for (SheetPostSearchIndex entity : entities) {
                    delete(entity);
                }
            }

            @Override
            public void deleteAll() {
                store.clear();
            }
        };
    }

    @Bean
    @Primary
    public ElasticSearchInstance elasticSearchInstance() {
        return new ElasticSearchInstance(null) {
            @Override
            public void invertIndexingSheetPost(SheetPost sheetPost) {
                sheetPostIndexRepository().save(SheetPostIndex.of(sheetPost));
            }

            @Override
            public void invertIndexingSheetPost(SheetPostIndex index) {
                sheetPostIndexRepository().save(index);
            }

            @Override
            public Pair<Page<Long>, String> searchSheetPost(String searchSentence, List<String> instruments,
                                                            List<String> difficulties, List<String> genres,
                                                            Pageable pageable) {
                List<Long> ids = new ArrayList<>();
                for (SheetPostIndex index : sheetPostIndexRepository().findAll()) {
                    ids.add(index.getId());
                }
                ids.sort(Comparator.reverseOrder());

                int from = (int) pageable.getOffset();
                if (from >= ids.size()) {
                    return Pair.of(Page.empty(pageable), "{}");
                }
                int to = Math.min(from + pageable.getPageSize(), ids.size());
                List<Long> content = ids.subList(from, to);
                return Pair.of(new PageImpl<>(content, pageable, ids.size()), "{}");
            }

            @Override
            public List<SheetPostIndex> searchPopularDateRangeSheetPost(DateRange dateRange, String limit)
                    throws IOException, TimeoutException {
                return List.of();
            }

            @Override
            public void savePostIndex(Post post) {
            }

            @Override
            public void updatePostIndex(Post post) {
            }

            @Override
            public void deletePostIndex(Long postId) {
            }

            @Override
            public Map<Long, Integer> getViewCountsBySheetPostIds(List<Long> sheetPostIds) {
                return new HashMap<>();
            }

            @Override
            public List<SheetPostIndex> getSearchSheetPostAutoComplete(String searchSentence, List<String> instruments,
                                                                       List<String> difficulties, List<String> genres) {
                return List.of();
            }
        };
    }
}
