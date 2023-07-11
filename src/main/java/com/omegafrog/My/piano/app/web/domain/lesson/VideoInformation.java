package com.omegafrog.My.piano.app.web.domain.lesson;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;
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
