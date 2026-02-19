package com.omegafrog.My.piano.app.web.infra.upload;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.upload.UploadJob;
import com.omegafrog.My.piano.app.web.domain.upload.UploadJobRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUploadJobRepositoryImpl implements UploadJobRepository {

    private final SimpleJpaUploadJobRepository jpaRepository;

    @Override
    public UploadJob save(UploadJob uploadJob) {
        return jpaRepository.save(uploadJob);
    }

    @Override
    public Optional<UploadJob> findByUploadId(String uploadId) {
        return jpaRepository.findById(uploadId);
    }
}
