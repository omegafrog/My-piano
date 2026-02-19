package com.omegafrog.My.piano.app.web.infra.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.omegafrog.My.piano.app.web.domain.user.QSecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.QUser;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaPostRepositoryImpl implements PostRepository {

	private final JPAQueryFactory factory;

	private final SimpleJpaPostRepository postRepository;

	@Override
	public Post save(Post post) {
		return postRepository.save(post);
	}

	@Override
	public Post saveAndFlush(Post post) {
		return postRepository.saveAndFlush(post);
	}

	@Override
	public Optional<Post> findById(Long id) {
		return Optional.ofNullable(factory.select(QPost.post).from(QPost.post)
			.leftJoin(QPost.post.author, QUser.user).fetchJoin()
			.leftJoin(QPost.post.author.securityUser, QSecurityUser.securityUser).fetchJoin()
			.where(QPost.post.id.eq(id))
			.fetchOne());
	}

	@Override
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
			.where(expression);
		List<Post> fetched = query
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createdAt.desc()).fetch();
		return PageableExecutionUtils.getPage(fetched, pageable, () -> query.fetch().size());

	}

	@Override
	public Long count() {
		return postRepository.count();
	}
}
