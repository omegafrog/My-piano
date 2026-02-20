package com.omegafrog.My.piano.app.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Component
@Profile("!test")
@Slf4j
public class StagePdfFileReaper {

    private final Path stageBasePath;
    private final Duration ttl;
    private final Clock clock;

    public StagePdfFileReaper(
            @Value("${file-upload.stage.base-path:./static/stage}") String stageBasePath,
            @Value("${file-upload.stage.ttl-hours:24}") long ttlHours
    ) {
        this(stageBasePath, Duration.ofHours(ttlHours), Clock.systemDefaultZone());
    }

    StagePdfFileReaper(String stageBasePath, Duration ttl, Clock clock) {
        this.stageBasePath = Path.of(stageBasePath);
        this.ttl = ttl;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void reap() {
        int deleted = reapExpired(clock.instant());
        if (deleted > 0) {
            log.info("Reaped staged PDFs. deletedCount: {}, ttlHours: {}", deleted, ttl.toHours());
        }
    }

    int reapForTest() {
        return reapExpired(clock.instant());
    }

    private int reapExpired(Instant now) {
        if (!Files.exists(stageBasePath)) {
            return 0;
        }

        Instant cutoff = now.minus(ttl);
        int deleted = 0;

        try (Stream<Path> stream = Files.list(stageBasePath)) {
            for (Path path : stream.toList()) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                FileTime lastModified;
                try {
                    lastModified = Files.getLastModifiedTime(path);
                } catch (IOException e) {
                    continue;
                }

                if (lastModified.toInstant().isAfter(cutoff)) {
                    continue;
                }

                try {
                    Files.deleteIfExists(path);
                    deleted++;
                } catch (IOException e) {
                    log.warn("Failed to delete expired staged PDF: {}", path.toAbsolutePath(), e);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to list staged PDF directory: {}", stageBasePath.toAbsolutePath(), e);
        }

        return deleted;
    }
}
