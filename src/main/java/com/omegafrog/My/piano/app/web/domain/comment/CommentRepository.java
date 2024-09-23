package com.omegafrog.My.piano.app.web.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);
    Optional<Comment> findById(Long id);

    Page<Comment> findAllByTargetId(Long targetId, Pageable pageable);
    void deleteById(Long id);
}
