package com.omegafrog.My.piano.app.post.infrastructure;

import com.omegafrog.My.piano.app.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleJpaPostRepository extends JpaRepository<Post, Long> {
}
