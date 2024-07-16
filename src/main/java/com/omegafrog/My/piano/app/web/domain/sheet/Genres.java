package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.enums.Genre;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Embeddable
@NoArgsConstructor
@Getter
public class Genres implements Serializable {
    private Genre genre1;
    private Genre genre2;

    @Builder
    public Genres(Genre genre1, Genre genre2) {
        this.genre1 = genre1;
        this.genre2 = genre2;
    }

    public List<Genre> getAll() {
        List<Genre> result = new ArrayList<>();
        if (genre1!=null) result.add(genre1);
        if (genre2!=null) result.add(genre2);
        return result;
    }
}
