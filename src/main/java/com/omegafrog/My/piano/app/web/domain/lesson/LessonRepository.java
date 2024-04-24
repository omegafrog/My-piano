package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository{

    Lesson save(Lesson lesson);

    Optional<Lesson> findById(Long id);

    void deleteById(Long id);

    Page<Lesson> findAll(Pageable pageable, SearchLessonFilter searchLessonFilter);
    List<Lesson> findAll(Pageable pageable);
}
