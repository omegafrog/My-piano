package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.comment.QComment;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheet;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.QUser;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class JpaSheetPostRepositoryImpl implements SheetPostRepository {
    @Autowired
    private SimpleJpaSheetPostRepository jpaRepository;

    private final JPAQueryFactory factory;

    @Override
    public SheetPost save(SheetPost sheetPost) {
        return jpaRepository.save(sheetPost);
    }

    @Override
    public Optional<SheetPost> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<SheetPost> findBySheetId(Long sheetId) {
        return jpaRepository.findBySheet_id(sheetId);
    }

    @Override
    public Page<SheetPost> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<SheetPost> findAll(Pageable pageable, SearchSheetPostFilter filter) {
        QSheetPost sheetPost = QSheetPost.sheetPost;
        BooleanExpression expressions = filter.getExpressions();
        JPAQuery<SheetPost> query = factory.selectFrom(sheetPost)
                .where(expressions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sheetPost.createdAt.desc());

        return PageableExecutionUtils.getPage(query.fetch(), pageable, this::count);

    }

    @Override
    public Page<SheetPost> findByIds(List<Long> sheetPostIds, Pageable pageable) {
        QSheetPost sheetPost = QSheetPost.sheetPost;
        QSheet sheet = QSheet.sheet;
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        BooleanExpression expressions = sheetPost.id.in(sheetPostIds);
        JPAQuery<SheetPost> query = factory.selectFrom(sheetPost)
                .join(sheetPost.sheet, sheet).fetchJoin()
                .leftJoin(sheetPost.comments, comment).fetchJoin()
                .leftJoin(sheetPost.author, user)
                .where(expressions)
                .orderBy(sheetPost.createdAt.desc());

        return PageableExecutionUtils.getPage(query.fetch(),pageable , () -> factory.select(sheetPost.count())
                .from(sheetPost)
                .fetchOne());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }


    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public Long count() {
        return jpaRepository.count();
    }

}
