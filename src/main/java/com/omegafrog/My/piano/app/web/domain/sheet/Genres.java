package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.enums.Genre;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class Genres {
    private Genre genre1;
    private Genre genre2;

    @Builder
    public Genres(Genre genre1, Genre genre2) {
        this.genre1 = genre1;
        this.genre2 = genre2;
    }
}
