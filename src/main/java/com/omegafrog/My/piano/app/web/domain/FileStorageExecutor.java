package com.omegafrog.My.piano.app.web.domain;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Component
public class FileStorageExecutor {

    private final S3UploadFileExecutor s3UploadFileExecutor;
    private final LocalFileStorageExecutor localFileStorageExecutor;

    public FileStorageExecutor(S3UploadFileExecutor s3UploadFileExecutor, LocalFileStorageExecutor localFileStorageExecutor) {
        this.s3UploadFileExecutor = s3UploadFileExecutor;
        this.localFileStorageExecutor = localFileStorageExecutor;
    }

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.uploadSheet(file, filename);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.uploadSheet(file, filename, metadata);
        }
    }

    public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) throws IOException {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.uploadThumbnail(document, filename);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.uploadThumbnail(document, filename, metadata);
        }
    }

    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata) throws IOException {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.uploadProfileImg(profileImg, filename);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.uploadProfileImg(profileImg, filename, metadata);
        }
    }

    public void removeProfileImg(String url) {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.removeProfileImg(url);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.removeProfileImg(url);
        }
    }

    public void removeSheetPost(SheetPost sheetPost) {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.removeSheetPost(sheetPost);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.removeSheetPost(sheetPost);
        }
    }

    public URL createFileUrl(String sheetUrl) {
        if ("dev".equals(activeProfile)) {
            return localFileStorageExecutor.createFileUrl(sheetUrl);
        } else if (s3UploadFileExecutor != null) {
            return s3UploadFileExecutor.createFileUrl(sheetUrl);
        }
        return null;
    }

    public void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId) throws IOException {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.uploadSheetAsync(file, filename, uploadId);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.uploadSheetAsync(file, filename, metadata, uploadId);
        }
    }

    public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId) throws IOException {
        if ("dev".equals(activeProfile)) {
            localFileStorageExecutor.uploadThumbnailAsync(document, filename, uploadId);
        } else if (s3UploadFileExecutor != null) {
            s3UploadFileExecutor.uploadThumbnailAsync(document, filename, metadata, uploadId);
        }
    }
}