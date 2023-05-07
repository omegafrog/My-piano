package com.omegafrog.My.piano.app.post.entity;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@Entity
@Table(name = "`USER`")
@NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    private String profileSrc;

    @Builder
    public Author(String name, String profileSrc) {
        this.name = name;
        this.profileSrc = profileSrc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        if (!id.equals(author.id)) return false;
        if (!name.equals(author.name)) return false;
        return Objects.equals(profileSrc, author.profileSrc);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (profileSrc != null ? profileSrc.hashCode() : 0);
        return result;
    }
}
