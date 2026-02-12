package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StageStorageUsage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.util.stream.Stream;

@Component
@Slf4j
public class LocalStagePdfStorage implements StagePdfStorage {

    private final Path basePath;
    private final Clock clock;

    public LocalStagePdfStorage(
            @Value("${file-upload.stage.base-path:./static/stage}") String basePath
    ) {
        this(basePath, Clock.systemDefaultZone());
    }

    LocalStagePdfStorage(String basePath, Clock clock) {
        this.basePath = Path.of(basePath);
        this.clock = clock;
    }

    @Override
    public StagedPdf stage(MultipartFile file, String uploadId) throws IOException {
        Files.createDirectories(basePath);

        Path target = basePath.resolve(uploadId + ".pdf");
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        long sizeBytes = Files.size(target);
        Instant stagedAt = clock.instant();
        log.info("Staged PDF saved. uploadId: {}, path: {}, sizeBytes: {}", uploadId, target.toAbsolutePath(), sizeBytes);
        return new StagedPdf(target.toAbsolutePath().toString(), sizeBytes, stagedAt);
    }

    @Override
    public boolean deleteIfExists(String stagePath) {
        try {
            Path path = Path.of(stagePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete staged file: {}", stagePath, e);
            return false;
        }
    }

    @Override
    public StageStorageUsage usage() throws IOException {
        if (!Files.exists(basePath)) {
            return new StageStorageUsage(0, 0);
        }

        long fileCount = 0;
        long totalBytes = 0;
        try (Stream<Path> stream = Files.list(basePath)) {
            for (Path p : stream.toList()) {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                fileCount++;
                try {
                    totalBytes += Files.size(p);
                } catch (IOException ignored) {
                }
            }
        }

        return new StageStorageUsage(fileCount, totalBytes);
    }

    Path basePath() {
        return basePath;
    }
}
