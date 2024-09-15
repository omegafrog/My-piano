package com.omegafrog.My.piano.app.web.domain.lesson;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class VideoInformation implements Serializable {
    private String videoUrl;
    private LocalTime runningTime;

    @Builder
    public VideoInformation(String videoUrl, LocalTime runningTime) {
        this.videoUrl = videoUrl;
        this.runningTime = runningTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoInformation that = (VideoInformation) o;
        return Objects.equals(getVideoUrl(), that.getVideoUrl()) && Objects.equals(getRunningTime(), that.getRunningTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVideoUrl(), getRunningTime());
    }
}
