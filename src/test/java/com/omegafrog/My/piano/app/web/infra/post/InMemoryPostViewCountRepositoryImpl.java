package com.omegafrog.My.piano.app.web.infra.post;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCountRepository;

@Repository
@Profile("test")
public class InMemoryPostViewCountRepositoryImpl implements PostViewCountRepository, TestResettable {

    private final Map<Long, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public int incrementViewCount(Article article) {
        if (!(article instanceof Post post)) {
            throw new IllegalArgumentException("Article is not Post");
        }
        return counts.merge(post.getId(), post.getViewCount() + 1, (prev, init) -> prev + 1);
    }

    @Override
    public PostViewCount findById(Long id) {
        Integer count = counts.get(id);
        if (count == null) {
            throw new IllegalArgumentException("Cannot find PostViewCount entity");
        }
        return new PostViewCount(id, count);
    }

    @Override
    public boolean exist(Long id) {
        return counts.containsKey(id);
    }

    @Override
    public PostViewCount save(PostViewCount viewCount) {
        counts.put(viewCount.getId(), viewCount.getViewCount());
        return viewCount;
    }

    @Override
    public void reset() {
        counts.clear();
    }
}
