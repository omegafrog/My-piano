package com.omegafrog.My.piano.app.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class StagePdfFileReaperTest {

    @TempDir
    Path tempDir;

    @Test
    void deletesOnlyExpiredStagedFiles() throws IOException {
        Instant now = Instant.parse("2026-02-12T00:00:00Z");
        Clock clock = Clock.fixed(now, ZoneId.of("UTC"));

        Path expired = tempDir.resolve("expired.pdf");
        Files.write(expired, "x".getBytes());
        Files.setLastModifiedTime(expired, java.nio.file.attribute.FileTime.from(now.minus(Duration.ofHours(25))));

        Path fresh = tempDir.resolve("fresh.pdf");
        Files.write(fresh, "y".getBytes());
        Files.setLastModifiedTime(fresh, java.nio.file.attribute.FileTime.from(now.minus(Duration.ofHours(1))));

        StagePdfFileReaper reaper = new StagePdfFileReaper(tempDir.toString(), Duration.ofHours(24), clock);
        int deletedCount = reaper.reapForTest();

        assertThat(deletedCount).isEqualTo(1);
        assertThat(Files.exists(expired)).isFalse();
        assertThat(Files.exists(fresh)).isTrue();
    }
}
