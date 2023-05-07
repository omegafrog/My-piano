package com.omegafrog.My.piano.app.lesson.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Lesson save(Lesson lesson);

    Optional<Lesson> findById(Long id);

    void deleteById(Long id);
}