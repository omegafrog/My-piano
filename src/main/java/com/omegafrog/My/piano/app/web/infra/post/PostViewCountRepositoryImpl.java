package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

@RequiredArgsConstructor
@Repository
@Profile("!test")
public class PostViewCountRepositoryImpl implements PostViewCountRepository {
    private static final String CACHE_NAME = "postViewCounts";
    private final CacheManager cacheManager;
    private final PostRepository postRepository;

    private Cache<Long, Integer> cache() {
        return cacheManager.getCache(CACHE_NAME, Long.class, Integer.class);
    }

    @Override
    public int incrementViewCount(Article article) {
        if (!(article instanceof Post post)) {
            throw new IllegalArgumentException("Article is not Post");
        }
        Integer updated = cache().invoke(post.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = current + 1;
                    entry.setValue(next);
                    return next;
                }, post.getViewCount());
        return updated == null ? post.getViewCount() + 1 : updated;
    }

    @Override
    public PostViewCount findById(Long id) {
        Integer cached = cache().get(id);
        if (cached != null) {
            return new PostViewCount(id, cached);
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Post entity"));
        int count = post.getViewCount();
        cache().put(id, count);
        return new PostViewCount(id, count);
    }

    @Override
    public boolean exist(Long id) {
        return cache().containsKey(id);
    }


    @Override
    public PostViewCount save(PostViewCount viewCount) {
        cache().put(viewCount.getId(), viewCount.getViewCount());
        return viewCount;
    }
}
