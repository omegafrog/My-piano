package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StageStorageUsage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStagePdfStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void stageSavesPdfFileAndUpdatesUsage() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-02-12T00:00:00Z"), ZoneId.of("UTC"));
        StagePdfStorage storage = new LocalStagePdfStorage(tempDir.toString(), clock);

        byte[] bytes = "pdf-bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "score.pdf", "application/pdf", bytes);

        StagedPdf staged = storage.stage(file, "upload-123");
        Path stagedPath = Path.of(staged.stagePath());

        assertThat(Files.exists(stagedPath)).isTrue();
        assertThat(stagedPath.getFileName().toString()).isEqualTo("upload-123.pdf");
        assertThat(Files.readAllBytes(stagedPath)).isEqualTo(bytes);

        StageStorageUsage usage = storage.usage();
        assertThat(usage.fileCount()).isEqualTo(1);
        assertThat(usage.totalBytes()).isEqualTo(bytes.length);

        boolean deleted = storage.deleteIfExists(staged.stagePath());
        assertThat(deleted).isTrue();
        assertThat(Files.exists(stagedPath)).isFalse();

        StageStorageUsage after = storage.usage();
        assertThat(after.fileCount()).isEqualTo(0);
        assertThat(after.totalBytes()).isEqualTo(0);
    }
}
