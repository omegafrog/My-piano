package com.omegafrog.My.piano.app.lesson.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.time.LocalTime;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class VideoInformation {
    private String videoUrl;
    private LocalTime runningTime;

    @Builder
    public VideoInformation(String videoUrl, LocalTime runningTime) {
        this.videoUrl = videoUrl;
        this.runningTime = runningTime;
    }
}
