package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleJpaPostRepository extends JpaRepository<Post, Long> {
}
