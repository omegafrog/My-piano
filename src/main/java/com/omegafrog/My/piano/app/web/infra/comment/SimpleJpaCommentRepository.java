package com.omegafrog.My.piano.app.web.infra.comment;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleJpaCommentRepository extends JpaRepository<Comment, Long> {
}
