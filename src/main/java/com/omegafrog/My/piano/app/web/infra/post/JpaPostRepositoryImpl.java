package com.omegafrog.My.piano.app.web.infra.post;

import com.omegafrog.My.piano.app.security.entity.QSecurityUser;
import com.omegafrog.My.piano.app.web.domain.comment.QComment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.user.QUser;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPostRepositoryImpl implements PostRepository {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private final JPAQueryFactory factory;

    private final SimpleJpaPostRepository postRepository;
    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(factory.select(QPost.post).from(QPost.post)
                .join(QPost.post.author, QUser.user).fetchJoin()
                        .join(QPost.post.author.securityUser, QSecurityUser.securityUser).fetchJoin()
                .where(QPost.post.id.eq(id))
                .fetchOne());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Post byId = findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Post entity."));
        byId.getAuthor().deleteUploadedPost(byId);

//        postRepository.deleteAllLikedPostById(id);
        postRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        postRepository.deleteAll();
    }

    @Override
    public List<Post> findAll(Pageable pageable, Sort sort) {
        return postRepository.findAll(pageable).stream().sorted((o1, o2) -> {
            if (o1.getCreatedAt().isAfter(o2.getCreatedAt()))
                return -1;
            else if (o1.getCreatedAt().isBefore(o2.getCreatedAt()))
                return 1;
            else
                return 0;
        }).toList();
    }

    @Override
    public Page<Post> findAll(SearchPostFilter filter, Pageable pageable) {
        QPost post = QPost.post;
        BooleanExpression expression = filter.getExpression();
        JPAQuery<Post> query = factory.selectFrom(post)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(post.createdAt.desc());
        List<Post> fetched = query.fetch();
        Long count = query.fetchCount();
        return PageableExecutionUtils.getPage(fetched, pageable, count::longValue);

    }

    @Override
    public Long count() {
        return postRepository.count();
    }
}
