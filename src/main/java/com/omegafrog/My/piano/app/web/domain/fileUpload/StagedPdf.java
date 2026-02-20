package com.omegafrog.My.piano.app.web.domain.fileUpload;

import java.time.Instant;

public record StagedPdf(
        String stagePath,
        long sizeBytes,
        Instant stagedAt
) {
}
