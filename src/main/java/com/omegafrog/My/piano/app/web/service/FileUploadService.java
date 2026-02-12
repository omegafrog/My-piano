package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.dto.fileUpload.FileUploadResponse;
import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
import com.omegafrog.My.piano.app.web.exception.WrongFileExtensionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final FileStorageExecutor fileStorageExecutor;
    private final FileUploadJobRepository fileUploadJobRepository;
    private final SheetPostRepository sheetPostRepository;
    private final StagePdfStorage stagePdfStorage;

    @Qualifier("CommonUserRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

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
            String redisKey = "file-upload:" + uploadId;
            Map<String, String> uploadHash = new HashMap<>();
            uploadHash.put("status", FileUploadStatus.PENDING.name());
            uploadHash.put("sheetPostId", "");
            uploadHash.put("sheetUrl", "");
            uploadHash.put("thumbnailUrl", "");
            uploadHash.put("originalFileName", originalFilename);
            uploadHash.put("uuidFileName", uuidFilename);
            uploadHash.put("pageNum", "");
            uploadHash.put("createdAt", LocalDateTime.now().toString());
            uploadHash.put("completedAt", "");

            redisTemplate.opsForHash().putAll(redisKey, uploadHash);
            redisTemplate.expire(redisKey, Duration.ofHours(1));

            // Stage PDF file first (persistent) then enqueue job
            StagedPdf stagedPdf = stagePdfStorage.stage(file, uploadId);

            FileUploadJob uploadJob = FileUploadJob.builder()
                    .uploadId(uploadId)
                    .originalFileName(originalFilename)
                    .uuidFileName(uuidFilename)
                    .stagedFilePath(stagedPdf.stagePath())
                    .status(FileUploadJobStatus.PENDING)
                    .maxAttempts(DEFAULT_MAX_ATTEMPTS)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            fileUploadJobRepository.save(uploadJob);

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadStatus.PENDING)
                    .message("파일 업로드가 대기열에 등록되었습니다.")
                    .originalFileName(originalFilename)
                    .build();

        } catch (Exception e) {
            log.error("Failed to start file upload for uploadId: {}", uploadId, e);

            // Redis Hash에서 실패 상태로 업데이트
            String redisKey = "file-upload:" + uploadId;
            redisTemplate.opsForHash().put(redisKey, "status", FileUploadStatus.FAILED.name());
            redisTemplate.expire(redisKey, Duration.ofHours(1));

            return FileUploadResponse.builder()
                    .uploadId(uploadId)
                    .status(FileUploadStatus.FAILED)
                    .message("파일 업로드 시작에 실패했습니다: " + e.getMessage())
                    .originalFileName(originalFilename)
                    .build();
        }
    }

    public void updateUploadMapping(String uploadId, Long sheetPostId) {
        String redisKey = "file-upload:" + uploadId;
        redisTemplate.opsForHash().put(redisKey, "sheetPostId", sheetPostId.toString());
        log.info("Updated Redis mapping: uploadId {} -> sheetPostId {}", uploadId, sheetPostId);
    }

    public String getSheetPostIdByUploadId(String uploadId) {
        String redisKey = "file-upload:" + uploadId;
        String value = (String) redisTemplate.opsForHash().get(redisKey, "sheetPostId");
        log.info("Retrieved from Redis: uploadId {} -> sheetPostId {}", uploadId, value);
        return (value != null && !value.isEmpty()) ? value : null;
    }

    public FileUploadStatus getUploadStatus(String uploadId) {
        String redisKey = "file-upload:" + uploadId;
        String status = (String) redisTemplate.opsForHash().get(redisKey, "status");

        if (status == null || status.isEmpty()) {
            return null; // TTL 만료 또는 존재하지 않음
        }

        try {
            return FileUploadStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void updateUploadData(String uploadId, String sheetUrl, String thumbnailUrl, int pageNum) {
        String redisKey = "file-upload:" + uploadId;
        Map<String, String> updates = new HashMap<>();

        if (sheetUrl != null) {
            updates.put("sheetUrl", sheetUrl);
        }
        if (thumbnailUrl != null) {
            updates.put("thumbnailUrl", thumbnailUrl);
        }
        if (pageNum > 0) {
            updates.put("pageNum", String.valueOf(pageNum));
        }

        updates.put("status", FileUploadStatus.COMPLETED.name());
        updates.put("completedAt", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(redisKey, updates);
        log.info("Updated upload data for uploadId: {}, sheetUrl: {}, thumbnailUrl: {}, pageNum: {}",
                uploadId, sheetUrl, thumbnailUrl, pageNum);
    }

    public void updateUploadStatus(String uploadId, FileUploadStatus status) {
        String redisKey = "file-upload:" + uploadId;
        redisTemplate.opsForHash().put(redisKey, "status", status.name());
        if (status == FileUploadStatus.COMPLETED || status == FileUploadStatus.FAILED) {
            redisTemplate.opsForHash().put(redisKey, "completedAt", LocalDateTime.now().toString());
        }
    }

    @Transactional
    public boolean applyUploadDataToSheetPost(String uploadId) {
        Map<String, String> uploadData = getUploadData(uploadId);
        if (uploadData == null) {
            return false;
        }

        String sheetPostIdStr = uploadData.get("sheetPostId");
        if (sheetPostIdStr == null || sheetPostIdStr.isEmpty()) {
            return false;
        }

        Long sheetPostId;
        try {
            sheetPostId = Long.parseLong(sheetPostIdStr);
        } catch (NumberFormatException e) {
            log.warn("SheetPostId is not a number for uploadId: {}, value: {}", uploadId, sheetPostIdStr);
            return false;
        }

        SheetPost sheetPost = sheetPostRepository.findById(sheetPostId)
                .orElseThrow(() -> new RuntimeException("SheetPost not found: " + sheetPostId));

        String sheetUrl = uploadData.get("sheetUrl");
        String thumbnailUrl = uploadData.get("thumbnailUrl");
        String pageNumStr = uploadData.get("pageNum");
        String originalFileName = uploadData.get("originalFileName");

        if (sheetUrl == null || sheetUrl.isEmpty() || thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return false;
        }

        sheetPost.getSheet().updateUrls(sheetUrl, thumbnailUrl);

        if (pageNumStr != null && !pageNumStr.isEmpty()) {
            try {
                sheetPost.getSheet().updatePageNum(Integer.parseInt(pageNumStr));
            } catch (NumberFormatException e) {
                log.warn("Invalid pageNum for uploadId: {}, value: {}", uploadId, pageNumStr);
            }
        }

        if (originalFileName != null && !originalFileName.isEmpty()) {
            sheetPost.getSheet().updateOriginalFileName(originalFileName);
        }

        sheetPostRepository.save(sheetPost);
        log.info("Applied upload data to SheetPost. uploadId: {}, sheetPostId: {}", uploadId, sheetPostId);
        return true;
    }

    public String buildSheetUrl(String filename) {
        return fileStorageExecutor.buildSheetUrl(filename);
    }

    public String buildThumbnailUrls(String filename, int pageNum) {
        return fileStorageExecutor.buildThumbnailUrls(filename, pageNum);
    }

    public Map<String, String> getUploadData(String uploadId) {
        String redisKey = "file-upload:" + uploadId;
        if (!redisTemplate.hasKey(redisKey)) {
            throw new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId);
        }
        Map<Object, Object> hashData = redisTemplate.opsForHash().entries(redisKey);

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hashData.entrySet()) {
            result.put((String) entry.getKey(), (String) entry.getValue());
        }

        return result.isEmpty() ? null : result;
    }

    public boolean isUploadCompleted(String uploadId) {
        String status = (String) redisTemplate.opsForHash().get("file-upload:" + uploadId, "status");
        return FileUploadStatus.COMPLETED.name().equals(status) || FileUploadStatus.LINKED.name().equals(status);
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
