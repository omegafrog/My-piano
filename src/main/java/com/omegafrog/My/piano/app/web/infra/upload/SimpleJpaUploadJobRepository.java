package com.omegafrog.My.piano.app.web.infra.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import com.omegafrog.My.piano.app.web.domain.upload.UploadJob;

public interface SimpleJpaUploadJobRepository extends JpaRepository<UploadJob, String> {
}
