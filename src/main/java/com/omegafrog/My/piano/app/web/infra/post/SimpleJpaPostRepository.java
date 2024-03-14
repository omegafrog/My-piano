package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SimpleJpaPostRepository extends JpaRepository<Post, Long> {

}
