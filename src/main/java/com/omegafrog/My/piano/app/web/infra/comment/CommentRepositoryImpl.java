package com.omegafrog.My.piano.app.web.infra.comment;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.comment.QComment;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {


    private final JPAQueryFactory queryFactory;
    private final JpaCommentRepository jpaRepository;

    public Comment save(Comment comment){
        return jpaRepository.save(comment);
    }

    public Optional<Comment> findById(Long commentId){
        return jpaRepository.findById(commentId);
    }
    public Page<Comment> findAllByTargetId(Long targetId, Pageable pageable){
        QComment comment = QComment.comment;
        assert comment.target !=null;
        JPAQuery<Comment> query = queryFactory.selectFrom(comment)
                .where(comment.target.id.eq(targetId))
                .offset(pageable.getOffset())
                .orderBy(comment.createdAt.desc())
                .limit(pageable.getPageSize());
        return PageableExecutionUtils.getPage(query.fetch(), pageable,
                () -> query.fetchCount());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
