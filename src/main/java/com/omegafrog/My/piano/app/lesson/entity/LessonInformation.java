package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.lesson.entity.enums.Category;
import com.omegafrog.My.piano.app.lesson.entity.enums.Instrument;
import com.omegafrog.My.piano.app.lesson.entity.enums.RefundPolicy;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
@NoArgsConstructor
public class LessonInformation {
    private String artistDescription;
    private String lessonDescription;
    private RefundPolicy policy;
    private Instrument instrument;
    private Category category;
    private LocalTime runningTime;


    @Builder
    public LessonInformation(String artistDescription, String lessonDescription, RefundPolicy policy, Instrument instrument, Category category, LocalTime runningTime) {
        this.artistDescription = artistDescription;
        this.lessonDescription = lessonDescription;
        this.policy = policy;
        this.instrument = instrument;
        this.category = category;
        this.runningTime = runningTime;
    }

}
