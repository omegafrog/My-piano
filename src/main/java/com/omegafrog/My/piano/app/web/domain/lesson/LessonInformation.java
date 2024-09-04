package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.enums.Category;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.enums.RefundPolicy;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Setter
@Getter
public class LessonInformation implements Serializable {
    private String artistDescription;
    private String lessonDescription;
    private RefundPolicy policy;
    private Instrument instrument;
    private Category category;


    @Builder
    public LessonInformation(String artistDescription, String lessonDescription, RefundPolicy policy, Instrument instrument, Category category) {
        this.artistDescription = artistDescription;
        this.lessonDescription = lessonDescription;
        this.policy = policy;
        this.instrument = instrument;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonInformation that = (LessonInformation) o;
        return Objects.equals(getArtistDescription(), that.getArtistDescription()) && Objects.equals(getLessonDescription(), that.getLessonDescription()) && getPolicy() == that.getPolicy() && getInstrument() == that.getInstrument() && getCategory() == that.getCategory();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtistDescription(), getLessonDescription(), getPolicy(), getInstrument(), getCategory());
    }
}
