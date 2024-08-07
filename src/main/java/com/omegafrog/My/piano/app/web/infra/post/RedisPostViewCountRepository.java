package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.post.PostViewCount;
import org.springframework.data.repository.CrudRepository;

public interface RedisPostViewCountRepository extends CrudRepository<PostViewCount, Long> {
}
