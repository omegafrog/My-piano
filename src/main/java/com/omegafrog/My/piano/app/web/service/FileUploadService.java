package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import com.omegafrog.My.piano.app.web.dto.fileUpload.FileUploadResponse;
import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
import com.omegafrog.My.piano.app.web.exception.WrongFileExtensionException;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final FileUploadJobRepository fileUploadJobRepository;
    private final StagePdfStorage stagePdfStorage;
    private final FileUploadRedisReadModelWriter readModelWriter;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        String uploadId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String uuidFilename = UUID.randomUUID().toString() + ".pdf";

        StagedPdf stagedPdf = stagePdfStorage.stage(file, uploadId);

        try {
            FileUploadJob job = FileUploadJob.builder()
                    .uploadId(uploadId)
                    .originalFileName(originalFilename)
                    .uuidFileName(uuidFilename)
                    .stagedFilePath(stagedPdf.stagePath())
                    .status(FileUploadJobStatus.PENDING)
                    .maxAttempts(DEFAULT_MAX_ATTEMPTS)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();

            FileUploadJob saved = fileUploadJobRepository.save(job);
            readModelWriter.upsert(saved);

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadStatus.PENDING)
                    .message("파일 업로드가 대기열에 등록되었습니다.")
                    .originalFileName(originalFilename)
                    .build();
        } catch (Exception e) {
            // job 저장 실패 시 staged 파일은 고아가 되지 않도록 삭제 시도
            stagePdfStorage.deleteIfExists(stagedPdf.stagePath());
            throw e;
        }
    }

    public FileUploadStatus getUploadStatus(String uploadId) {
        return fileUploadJobRepository.findByUploadId(uploadId)
                .map(job -> {
                    readModelWriter.upsert(job);
                    return toApiStatus(job);
                })
                .orElse(null);
    }

    private static FileUploadStatus toApiStatus(FileUploadJob job) {
        if (job.getStatus() == FileUploadJobStatus.PENDING || job.getStatus() == FileUploadJobStatus.RETRY) {
            return FileUploadStatus.PENDING;
        }
        if (job.getStatus() == FileUploadJobStatus.RUNNING) {
            return FileUploadStatus.UPLOADING;
        }
        if (job.getStatus() == FileUploadJobStatus.FAILED) {
            return FileUploadStatus.FAILED;
        }
        if (job.getStatus() == FileUploadJobStatus.COMPLETED) {
            return job.isLinked() ? FileUploadStatus.LINKED : FileUploadStatus.COMPLETED;
        }
        return FileUploadStatus.PENDING;
    }

    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new WrongFileExtensionException("PDF 파일만 업로드 가능합니다: " + filename);
        }
    }
}
