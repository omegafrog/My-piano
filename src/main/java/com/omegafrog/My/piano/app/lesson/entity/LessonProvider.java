package com.omegafrog.My.piano.app.lesson.entity;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "`USER`")
@NoArgsConstructor
public class LessonProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String profileSrc;

    @Builder
    public LessonProvider(String name, String profileSrc) {
        this.name = name;
        this.profileSrc = profileSrc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LessonProvider lessonProvider = (LessonProvider) o;

        return id.equals(lessonProvider.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
