package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.upload.UploadJob;
import com.omegafrog.My.piano.app.web.domain.upload.UploadJobRepository;
import com.omegafrog.My.piano.app.web.dto.fileUpload.FileUploadResponse;
import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
import com.omegafrog.My.piano.app.web.exception.WrongFileExtensionException;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileStorageExecutor fileStorageExecutor;

    private final FileUploadStatusStore statusStore;

    private final UploadJobRepository uploadJobRepository;

    private final SheetPostRepository sheetPostRepository;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();

        // 시간순 정렬 가능한 UUID 기반 파일명 생성 (확장자 포함)
        String fileExtension = getFileExtension(originalFilename);
        String uuidFilename = UUID.randomUUID().toString() + "." + fileExtension;

        log.info("Starting file upload process for uploadId: {}, original: {}, uuid: {}",
                uploadId, originalFilename, uuidFilename);

        try {
            // 파일 검증
            validateFile(file);

            // Redis Hash에 초기 상태 저장 (TTL 1시간)
            Map<String, String> uploadHash = new HashMap<>();
            uploadHash.put("status", FileUploadStatus.UPLOADING.name());
            uploadHash.put("sheetPostId", "");
            uploadHash.put("sheetUrl", "");
            uploadHash.put("thumbnailUrl", "");
            uploadHash.put("originalFileName", originalFilename);
            uploadHash.put("uuidFileName", uuidFilename);
            uploadHash.put("pageNum", "");
            uploadHash.put("createdAt", LocalDateTime.now().toString());
            uploadHash.put("completedAt", "");

            statusStore.putAll(uploadId, uploadHash, Duration.ofHours(1));

            uploadJobRepository.save(UploadJob.create(uploadId, originalFilename, uuidFilename, LocalDateTime.now()));

            // 임시 파일 생성 및 PDF 문서 로드
            SheetPostTempFile tempFile = createTempFile(file);

            // 비동기 파일 업로드 시작 (UUID 파일명으로 업로드)
            fileStorageExecutor.uploadSheetAsync(tempFile.temp(), uuidFilename, tempFile.metadata(), uploadId);
            fileStorageExecutor.uploadThumbnailAsync(tempFile.document(), uuidFilename,
                    new ObjectMetadata.Builder().contentType("jpg").build(), uploadId);

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadStatus.UPLOADING)
                    .message("파일 업로드가 시작되었습니다.")
                    .originalFileName(originalFilename)
                    .build();

        } catch (Exception e) {
            log.error("Failed to start file upload for uploadId: {}", uploadId, e);

            // Redis Hash에서 실패 상태로 업데이트
            statusStore.put(uploadId, "status", FileUploadStatus.FAILED.name());

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadStatus.FAILED)
                    .message("파일 업로드 시작에 실패했습니다: " + e.getMessage())
                    .originalFileName(originalFilename)
                    .build();
        }
    }

    public void updateUploadMapping(String uploadId, Long sheetPostId) {
        statusStore.put(uploadId, "sheetPostId", sheetPostId.toString());

        UploadJob uploadJob = uploadJobRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));
        uploadJob.assignSheetPostId(sheetPostId, LocalDateTime.now());
        uploadJobRepository.save(uploadJob);
        syncSheetPostIfPossible(uploadJob);

        log.info("Updated Redis mapping: uploadId {} -> sheetPostId {}", uploadId, sheetPostId);
    }

    public String getSheetPostIdByUploadId(String uploadId) {
        String value = statusStore.get(uploadId, "sheetPostId");
        if ((value == null || value.isEmpty())) {
            value = uploadJobRepository.findByUploadId(uploadId)
                    .map(UploadJob::getSheetPostId)
                    .map(String::valueOf)
                    .orElse(null);
        }
        log.info("Retrieved from Redis: uploadId {} -> sheetPostId {}", uploadId, value);
        return (value != null && !value.isEmpty()) ? value : null;
    }

    public FileUploadStatus getUploadStatus(String uploadId) {
        String status = statusStore.get(uploadId, "status");

        if (status == null || status.isEmpty()) {
            return uploadJobRepository.findByUploadId(uploadId)
                    .map(UploadJob::getStatus)
                    .orElse(null);
        }

        try {
            return FileUploadStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
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

        UploadJob uploadJob = uploadJobRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));

        uploadJob.mergeCompletedData(sheetUrl, thumbnailUrl, pageNum, originalFileName, now);
        uploadJobRepository.save(uploadJob);

        statusStore.putAll(uploadId, toUploadDataMap(uploadJob), Duration.ofHours(1));
        syncSheetPostIfPossible(uploadJob);

        log.info("Applied upload completed data. uploadId: {}, sheetUrl: {}, thumbnailUrl: {}, pageNum: {}",
                uploadId, sheetUrl, thumbnailUrl, pageNum);
    }

    @Transactional
    public void applyUploadFailed(
            String uploadId,
            String originalFileName,
            String errorMessage,
            String failureReason) {
        UploadJob uploadJob = uploadJobRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));

        uploadJob.markFailed(errorMessage, failureReason, LocalDateTime.now());
        uploadJobRepository.save(uploadJob);

        if (originalFileName != null && !originalFileName.isBlank()) {
            statusStore.put(uploadId, "originalFileName", originalFileName);
        }
        statusStore.put(uploadId, "status", FileUploadStatus.FAILED.name());
        statusStore.put(uploadId, "completedAt", "");

        log.warn("Applied upload failed data. uploadId: {}, reason: {}", uploadId, failureReason);
    }

    public Map<String, String> getUploadData(String uploadId) {
        if (statusStore.exists(uploadId)) {
            Map<String, String> result = statusStore.entries(uploadId);
            if (!result.isEmpty()) {
                return result;
            }
        }

        return uploadJobRepository.findByUploadId(uploadId)
                .map(this::toUploadDataMap)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId));
    }

    public boolean isUploadCompleted(String uploadId) {
        String status = statusStore.get(uploadId, "status");
        if (FileUploadStatus.COMPLETED.name().equals(status)) {
            return true;
        }

        return uploadJobRepository.findByUploadId(uploadId)
                .map(UploadJob::getStatus)
                .map(FileUploadStatus.COMPLETED::equals)
                .orElse(false);
    }

    private void syncSheetPostIfPossible(UploadJob uploadJob) {
        Long sheetPostId = uploadJob.getSheetPostId();
        if (sheetPostId == null) {
            return;
        }

        String sheetUrl = uploadJob.getSheetUrl();
        if (sheetUrl == null || sheetUrl.isBlank()) {
            return;
        }

        SheetPost sheetPost = sheetPostRepository.findById(sheetPostId)
                .orElseThrow(() -> new IllegalStateException("SheetPost not found: " + sheetPostId));

        Sheet sheet = sheetPost.getSheet();
        String thumbnailUrl = uploadJob.getThumbnailUrl();
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            thumbnailUrl = sheet.getThumbnailUrl();
        }

        sheet.updateUrls(sheetUrl, thumbnailUrl);

        if (uploadJob.getPageNum() > 0) {
            sheet.updatePageNum(uploadJob.getPageNum());
        }

        String originalFileName = uploadJob.getOriginalFileName();
        if (originalFileName != null && !originalFileName.isBlank()) {
            sheet.updateOriginalFileName(originalFileName);
        }
    }

    private Map<String, String> toUploadDataMap(UploadJob uploadJob) {
        Map<String, String> result = new HashMap<>();
        result.put("status", uploadJob.getStatus().name());
        result.put("sheetPostId", uploadJob.getSheetPostId() == null ? "" : uploadJob.getSheetPostId().toString());
        result.put("sheetUrl", uploadJob.getSheetUrl() == null ? "" : uploadJob.getSheetUrl());
        result.put("thumbnailUrl", uploadJob.getThumbnailUrl() == null ? "" : uploadJob.getThumbnailUrl());
        result.put("originalFileName", uploadJob.getOriginalFileName() == null ? "" : uploadJob.getOriginalFileName());
        result.put("uuidFileName", uploadJob.getUuidFileName() == null ? "" : uploadJob.getUuidFileName());
        result.put("pageNum", uploadJob.getPageNum() > 0 ? String.valueOf(uploadJob.getPageNum()) : "");
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

    private SheetPostTempFile createTempFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String contentType = filename.split("\\.")[1];
        ObjectMetadata metadata = ObjectMetadata.builder().contentType(contentType).build();

        File temp = File.createTempFile("upload-", ".pdf");
        try (FileOutputStream dest = new FileOutputStream(temp)) {
            dest.write(file.getBytes());
        }

        PDDocument document = Loader.loadPDF(temp);
        int pageNum = document.getNumberOfPages();
        temp.deleteOnExit();

        return new SheetPostTempFile(filename, metadata, temp, document, pageNum);
    }

    private record SheetPostTempFile(String filename, ObjectMetadata metadata, File temp,
            PDDocument document, int pageNum) {
    }
}
