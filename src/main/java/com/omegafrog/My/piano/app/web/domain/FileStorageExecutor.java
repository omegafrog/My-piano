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

    private final UploadFileExecutor uploadFileExecutor;

    public FileStorageExecutor(UploadFileExecutor s3UploadFileExecutor) {
        this.uploadFileExecutor = s3UploadFileExecutor;
    }

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        uploadFileExecutor.uploadSheet(file, filename, metadata);
    }

    public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) throws IOException {
        uploadFileExecutor.uploadThumbnail(document, filename, metadata);
    }

    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata)
            throws IOException {
        uploadFileExecutor.uploadProfileImg(profileImg, filename, metadata);
    }

    public void removeProfileImg(String url) {
        uploadFileExecutor.removeProfileImg(url);
    }

    public void removeSheetPost(SheetPost sheetPost) {
        uploadFileExecutor.removeSheetPost(sheetPost);
    }

    public URL createFileUrl(String sheetUrl) {
        return uploadFileExecutor.createFileUrl(sheetUrl);
    }

    public String buildSheetUrl(String filename) {
        return uploadFileExecutor.buildSheetUrl(filename);
    }

    public String buildThumbnailUrls(String filename, int pageNum) {
        return uploadFileExecutor.buildThumbnailUrls(filename, pageNum);
    }

    public void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId)
            throws IOException {
        uploadFileExecutor.uploadSheetAsync(file, filename, metadata, uploadId);
    }

    public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId)
            throws IOException {
        uploadFileExecutor.uploadThumbnailAsync(document, filename, metadata, uploadId);
    }
}
