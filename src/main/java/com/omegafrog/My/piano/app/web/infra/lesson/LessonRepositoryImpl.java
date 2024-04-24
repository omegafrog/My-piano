package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LessonRepositoryImpl implements LessonRepository {

    private final JpaLessonRepository jpaRepository;

    private final JPAQueryFactory factory;

    @Override
    public Lesson save(Lesson lesson) {
        return jpaRepository.save(lesson);
    }

    @Override
    public Optional<Lesson> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    @Override
    public List<Lesson> findAll(Pageable pageable){
        return jpaRepository.findAll(pageable).getContent();
    }

    @Override
    public Page<Lesson> findAll(Pageable pageable, SearchLessonFilter searchLessonFilter) {
        BooleanExpression expression = searchLessonFilter.getExpression();
        QLesson lesson = QLesson.lesson;
        JPAQuery<Lesson> query = factory.selectFrom(lesson)
                .where(expression)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(lesson.createdAt.desc());

        return PageableExecutionUtils.getPage(query.fetch(), pageable, () -> query.fetchCount());
    }
}
