package com.omegafrog.My.piano.app.web.infra.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersion;

public interface SimpleJpaPostIndexVersionRepository extends JpaRepository<PostIndexVersion, Long> {
}
