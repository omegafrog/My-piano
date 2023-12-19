package com.omegafrog.My.piano.app.web.domain.comment;

import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);
    Optional<Comment> findById(Long id);

}
