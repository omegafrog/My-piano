package com.omegafrog.My.piano.app.web.infra.sheetPost;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.cart.QCart;
import com.omegafrog.My.piano.app.web.domain.comment.QComment;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheet;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.QUser;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class JpaSheetPostRepositoryImpl implements SheetPostRepository {

	@Autowired
	private SimpleJpaSheetPostRepository jpaRepository;

	private final JPAQueryFactory factory;
	private final CacheManager cacheManager;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public SheetPost save(SheetPost sheetPost) {
		return jpaRepository.save(sheetPost);
	}

	@CachePut("sheetpost")
	public SheetPost update(SheetPost sheetPost) {
		return jpaRepository.save(sheetPost);
	}

	@Override
	public Optional<SheetPost> findById(Long id) {
		return Optional.ofNullable(factory.select(QSheetPost.sheetPost)
			.from(QSheetPost.sheetPost)
			.join(QSheetPost.sheetPost.author, QUser.user).fetchJoin()
			.leftJoin(QSheetPost.sheetPost.comments, QComment.comment).fetchJoin()
			.where(QSheetPost.sheetPost.id.eq(id))
			.fetchOne());
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
	public Page<SheetPostDto> findAll(Pageable pageable, SearchSheetPostFilter filter) {
		QSheetPost sheetPost = QSheetPost.sheetPost;
		BooleanExpression expressions = filter.getExpressions();
		JPAQuery<SheetPostDto> query = factory.select(
				Projections.constructor(SheetPostDto.class,
					sheetPost.id,
					sheetPost.title,
					sheetPost.content,
					sheetPost.author,
					sheetPost.sheet,
					sheetPost.createdAt,
					sheetPost.modifiedAt
				))
			.from(sheetPost)
			.where(expressions)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sheetPost.createdAt.desc());
		int count = factory.selectFrom(sheetPost)
			.where(expressions)
			.fetch().size();

		return PageableExecutionUtils.getPage(query.fetch(), pageable, () -> count);
	}

	@Override
	@Cacheable(cacheNames = "sheetpost", key = "#sheetPostIds")
	public List<SheetPostListDto> findByIds(List<Long> sheetPostIds, Pageable pageable) {
		QSheetPost sheetPost = QSheetPost.sheetPost;
		QUser user = QUser.user;
		QSheet sheet = QSheet.sheet;

		BooleanExpression expressions = sheetPost.id.in(sheetPostIds);
		JPAQuery<SheetPostListDto> query = factory.select
				(Projections.constructor(SheetPostListDto.class,
					sheetPost.id,
					sheetPost.title,
					user.name,
					user.profileSrc,
					sheet.title,
					sheet.difficulty,
					Projections.constructor(Genres.class,
						sheet.genres.genre1,
						sheet.genres.genre2),
					sheet.instrument,
					sheetPost.createdAt,
					sheetPost.price
				))
			.from(sheetPost)
			.join(sheetPost.author, user)
			.join(sheetPost.sheet, sheet)
			.where(expressions)
			.orderBy(sheetPost.createdAt.desc());

		return query.fetch();
	}

	@Override
	@CacheEvict(cacheNames = "sheetpost")
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}

	@CacheEvict(cacheNames = "sheetpost", allEntries = true)
	public void deleteAll() {
		jpaRepository.deleteAll();
	}

	@Override
	public Long count() {
		return jpaRepository.count();
	}

	@Override
	public List<SheetPost> findAllById(List<Long> list) {
		JPAQuery<SheetPost> query = factory.selectFrom(QSheetPost.sheetPost)
			.join(QSheetPost.sheetPost.author, QUser.user).fetchJoin()
			.join(QSheetPost.sheetPost.sheet, QSheet.sheet).fetchJoin()
			.join(QSheetPost.sheetPost.author.cart, QCart.cart).fetchJoin()
			.leftJoin(QSheetPost.sheetPost.comments, QComment.comment).fetchJoin()
			.where(QSheetPost.sheetPost.id.in(list));
		return query.fetch();
	}

	private Optional<SheetPost> getCachedSheetPost(Cache cache, Long id) {
		Cache.ValueWrapper valueWrapper = cache.get(id);
		return valueWrapper != null ? Optional.ofNullable((SheetPost)valueWrapper.get()) : Optional.empty();
	}
}
