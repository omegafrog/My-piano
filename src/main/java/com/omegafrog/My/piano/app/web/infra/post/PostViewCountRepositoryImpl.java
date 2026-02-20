package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import com.omegafrog.My.piano.app.web.domain.post.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
@Profile("!test")
public class PostViewCountRepositoryImpl implements PostViewCountRepository {
    private final RedisTemplate<String, PostViewCount> redisTemplate;
    private final RedisPostViewCountRepository repository;
    @Override
    public int incrementViewCount(Article article) {
        if(!(article instanceof Post post)) throw new IllegalArgumentException("Article is not Post");
        if(!exist(post.getId())){
            save(new PostViewCount(post.getId(), post.getViewCount()));
        }
        Long incremented = redisTemplate.opsForHash().increment("post:" + post.getId(), "viewCount", 1);
        return incremented.intValue();
    }

    @Override
    public PostViewCount findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cannot find PostViewCount entity"));
    }

    @Override
    public boolean exist(Long id) {
        return repository.existsById(id);
    }


    @Override
    public PostViewCount save(PostViewCount viewCount) {
        return repository.save(viewCount);
    }
}
