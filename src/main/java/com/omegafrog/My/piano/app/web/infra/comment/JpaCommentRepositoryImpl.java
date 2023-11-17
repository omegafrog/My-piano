package com.omegafrog.My.piano.app.web.infra.comment;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCommentRepositoryImpl implements CommentRepository {

    @Autowired
    private SimpleJpaCommentRepository jpaRepository;

    public Comment save(Comment comment){
        return jpaRepository.save(comment);
    }

    public Optional<Comment> findById(Long commentId){
        return jpaRepository.findById(commentId);
    }
}
