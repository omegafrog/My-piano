package com.omegafrog.My.piano.app.web.domain.outbox;

import java.util.Optional;

public interface PostIndexVersionRepository {

    Optional<PostIndexVersion> findByPostId(Long postId);

    PostIndexVersion save(PostIndexVersion postIndexVersion);
}
