package com.omegafrog.My.piano.app.web.domain.upload;

import java.util.Optional;

public interface UploadJobRepository {

    UploadJob save(UploadJob uploadJob);

    Optional<UploadJob> findByUploadId(String uploadId);
}
