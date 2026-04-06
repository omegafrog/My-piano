package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import com.omegafrog.My.piano.app.web.dto.fileUpload.FileUploadResponse;
import com.omegafrog.My.piano.app.web.exception.WrongFileExtensionException;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private static final int DEFAULT_RETRY_DELAY_SECONDS = 5;

    private final StagePdfStorage stagePdfStorage;
    private final FileUploadProcessRepository fileUploadProcessRepository;
    private final FileUploadRedisReadModelWriter readModelWriter;
    private final FileUploadLinkService fileUploadLinkService;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uuidFilename = UUID.randomUUID().toString() + "." + fileExtension;

        log.info("Starting file upload job for uploadId: {}, original: {}, uuid: {}",
                uploadId, originalFilename, uuidFilename);

        StagedPdf stagedPdf = null;
        try {
            validateFile(file);
            stagedPdf = stagePdfStorage.stage(file, uploadId);

            FileUploadProcess job = FileUploadProcess.builder()
                    .uploadId(uploadId)
                    .originalFileName(originalFilename)
                    .uuidFileName(uuidFilename)
                    .stagedFilePath(stagedPdf.stagePath())
                    .status(FileUploadProcessStatus.PENDING)
                    .maxAttempts(3)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();

            fileUploadProcessRepository.save(job);
            readModelWriter.upsert(job);

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadProcessStatus.PENDING)
                    .message("파일 업로드가 접수되었습니다.")
                    .originalFileName(originalFilename)
                    .build();
        } catch (Exception e) {
            log.error("Failed to start file upload job for uploadId: {}", uploadId, e);
            if (stagedPdf != null) {
                stagePdfStorage.deleteIfExists(stagedPdf.stagePath());
            }

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadProcessStatus.FAILED)
                    .message("파일 업로드 시작에 실패했습니다: " + e.getMessage())
                    .originalFileName(originalFilename)
                    .build();
        }
    }

    public void updateUploadMapping(String uploadId, Long sheetPostId) {
        fileUploadLinkService.linkUploadToSheetPost(uploadId, sheetPostId);
        log.info("Linked uploadId {} to sheetPostId {}", uploadId, sheetPostId);
    }

    public String getSheetPostIdByUploadId(String uploadId) {
        return fileUploadProcessRepository.findByUploadId(uploadId)
                .map(FileUploadProcess::getSheetPostId)
                .map(String::valueOf)
                .orElse(null);
    }

    public FileUploadProcessStatus getUploadStatus(String uploadId) {
        return fileUploadProcessRepository.findByUploadId(uploadId)
                .map(FileUploadProcess::getStatus)
                .orElse(null);
    }

    public void updateUploadData(String uploadId, String sheetUrl, String thumbnailUrl, int pageNum) {
        applyUploadCompleted(uploadId, sheetUrl, thumbnailUrl, null, pageNum);
    }

    @Transactional
    public void applyUploadCompleted(
            String uploadId,
            String sheetUrl,
            String thumbnailUrl,
            String originalFileName,
            int pageNum) {
        LocalDateTime now = LocalDateTime.now();

        FileUploadProcess uploadJob = fileUploadProcessRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));

        uploadJob.updateUploadResult(sheetUrl, thumbnailUrl, pageNum);
        uploadJob.markCompleted(now);
        fileUploadProcessRepository.save(uploadJob);
        readModelWriter.upsert(uploadJob);

        log.info("Applied upload completed data. uploadId: {}, sheetUrl: {}, thumbnailUrl: {}, pageNum: {}",
                uploadId, sheetUrl, thumbnailUrl, pageNum);
    }

    @Transactional
    public void applyUploadFailed(
            String uploadId,
            String originalFileName,
            String errorMessage,
            String failureReason) {
        FileUploadProcess uploadJob = fileUploadProcessRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));

        boolean willRetry = uploadJob.markRetryOrFailed(errorMessage, DEFAULT_RETRY_DELAY_SECONDS, LocalDateTime.now());
        fileUploadProcessRepository.save(uploadJob);
        readModelWriter.upsert(uploadJob);

        if (willRetry) {
            log.warn("Applied upload failed data with retry scheduled. uploadId: {}, reason: {}", uploadId,
                    failureReason);
            return;
        }

        log.warn("Applied upload failed data. uploadId: {}, reason: {}", uploadId, failureReason);
    }

    public Map<String, String> getUploadData(String uploadId) {
        return fileUploadProcessRepository.findByUploadId(uploadId)
                .map(this::toUploadDataMap)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));
    }

    public boolean isUploadCompleted(String uploadId) {
        return fileUploadProcessRepository.findByUploadId(uploadId)
                .map(FileUploadProcess::isUploadCompleted)
                .orElse(false);
    }

    public boolean isUploadLinked(String uploadId) {
        return fileUploadProcessRepository.findByUploadId(uploadId)
                .map(FileUploadProcess::isLinked)
                .orElse(false);
    }

    private Map<String, String> toUploadDataMap(FileUploadProcess uploadJob) {
        Map<String, String> result = new HashMap<>();
        result.put("status", uploadJob.getStatus().name());
        result.put("sheetPostId", uploadJob.getSheetPostId() == null ? "" : uploadJob.getSheetPostId().toString());
        result.put("sheetUrl", uploadJob.getSheetUrl() == null ? "" : uploadJob.getSheetUrl());
        result.put("thumbnailUrl", uploadJob.getThumbnailUrls() == null ? "" : uploadJob.getThumbnailUrls());
        result.put("originalFileName", uploadJob.getOriginalFileName() == null ? "" : uploadJob.getOriginalFileName());
        result.put("uuidFileName", uploadJob.getUuidFileName() == null ? "" : uploadJob.getUuidFileName());
        Integer pageNum = uploadJob.getPageNum();
        result.put("pageNum", pageNum != null && pageNum > 0 ? String.valueOf(pageNum) : "");
        result.put("createdAt", uploadJob.getCreatedAt() == null ? "" : uploadJob.getCreatedAt().toString());
        result.put("completedAt", uploadJob.getCompletedAt() == null ? "" : uploadJob.getCompletedAt().toString());
        return result;
    }

    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new WrongFileExtensionException("PDF 파일만 업로드 가능합니다: " + filename);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
