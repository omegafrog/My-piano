package com.omegafrog.My.piano.app.web.infra.comment;

import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCommentRepositoryImpl implements CommentRepository {

    @Autowired
    private SimpleJpaCommentRepository jpaRepository;

}
