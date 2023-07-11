package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.enums.Category;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.enums.RefundPolicy;
import lombok.Builder;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;

@Embeddable
@NoArgsConstructor
public class LessonInformation {
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

}
