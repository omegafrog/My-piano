package com.omegafrog.My.piano.app.web.infra.outbox;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersion;
import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaPostIndexVersionRepositoryImpl implements PostIndexVersionRepository {

    private final SimpleJpaPostIndexVersionRepository jpaRepository;

    @Override
    public Optional<PostIndexVersion> findByPostId(Long postId) {
        return jpaRepository.findById(postId);
    }

    @Override
    public PostIndexVersion save(PostIndexVersion postIndexVersion) {
        return jpaRepository.save(postIndexVersion);
    }
}
