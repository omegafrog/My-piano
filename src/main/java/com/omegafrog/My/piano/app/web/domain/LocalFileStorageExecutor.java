package com.omegafrog.My.piano.app.web.domain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.exception.CreateThumbnailFailedException;

import io.awspring.cloud.s3.ObjectMetadata;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFileStorageExecutor implements UploadFileExecutor {

    @Value("${local.storage.base-path}")
    private String basePath;

    @Override
    public void uploadSheet(File file, String filename, ObjectMetadata metadata) {
        log.info("upload sheet start - local storage");
        try {
            Path targetPath = Paths.get(basePath, "sheets", filename);
            Files.createDirectories(targetPath.getParent());
            Path tempPath = createSiblingTempFile(targetPath);
            try {
                Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);
                moveAtomically(tempPath, targetPath);
            } finally {
                Files.deleteIfExists(tempPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("upload sheet end - local storage");
    }

    @Override
    public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) {
        try {
            log.info("upload thumbnail start - local storage");
            Path thumbnailDir = Paths.get(basePath, "thumbnails");
            Files.createDirectories(thumbnailDir);

            List<BufferedImage> thumbnails = generateThumbnail(document, filename);
            for (int i = 0; i < thumbnails.size(); i++) {
                String thumbnailFilename = filename.substring(0, filename.lastIndexOf('.')) + "-" + i + ".jpg";
                Path targetPath = thumbnailDir.resolve(thumbnailFilename);
                Path tempPath = createSiblingTempFile(targetPath);
                try {
                    try (OutputStream outputStream = Files.newOutputStream(tempPath)) {
                        ImageIO.write(thumbnails.get(i), "jpg", outputStream);
                    }
                    moveAtomically(tempPath, targetPath);
                } finally {
                    Files.deleteIfExists(tempPath);
                }
            }
            log.info("upload thumbnail end - local storage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BufferedImage> generateThumbnail(PDDocument document, String pdfFilePath) {
        try {
            int pages = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);
            List<BufferedImage> thumbnailsFiles = new ArrayList<>();
            int dpi = 72;

            BufferedImage firstImg = renderer.renderImageWithDPI(0, dpi);
            int width = firstImg.getWidth();
            int height = firstImg.getHeight();

            int visibleHeight = (int) (height * 0.4);
            BufferedImage visibleImage = firstImg.getSubimage(0, 0, width, visibleHeight);
            BufferedImage blurredImage = blurImage(firstImg.getSubimage(0, visibleHeight, width, height - visibleHeight));

            BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = combinedImage.createGraphics();
            g.drawImage(visibleImage, 0, 0, null);
            g.drawImage(blurredImage, 0, visibleHeight, null);
            g.dispose();

            thumbnailsFiles.add(combinedImage);

            for (int i = 1; i < pages; i++) {
                log.info("이미지로  변환 시작: {}-{}", pdfFilePath, i);

                BufferedImage img = renderer.renderImageWithDPI(i, dpi);

                blurredImage = blurImage(img);
                thumbnailsFiles.add(blurredImage);
            }
            document.close();

            return thumbnailsFiles;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CreateThumbnailFailedException(e);
        }
    }

    private float[] matrix = new float[225];
    private ConvolveOp op = null;

    @PostConstruct
    public void init() {
        for (int i = 0; i < 225; ++i) {
            matrix[i] = 1.0f / 225.f;
        }
        op = new ConvolveOp(new Kernel(15, 15, matrix), ConvolveOp.EDGE_NO_OP, null);
    }

    private BufferedImage blurImage(BufferedImage image) {
        return op.filter(image, null);
    }

    private Path createSiblingTempFile(Path targetPath) throws IOException {
        Path directory = targetPath.getParent();
        Files.createDirectories(directory);
        return Files.createTempFile(directory, targetPath.getFileName().toString(), ".tmp");
    }

    private void moveAtomically(Path tempPath, Path targetPath) throws IOException {
        Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata) {
        log.info("upload profile start - local storage");
        try {
            Path profileDir = Paths.get(basePath, "profiles");
            Files.createDirectories(profileDir);
            Path targetPath = profileDir.resolve(filename);

            try (InputStream inputStream = profileImg.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("upload profile end - local storage");
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void removeProfileImg(String filename) {
        log.info("remove profile start - local storage");
        try {
            Path filePath = Paths.get(basePath, "profiles", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete profile image: {}", filename, e);
        }
        log.info("remove profile end - local storage");
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void removeSheetPost(SheetPost sheetPost) {
        String sheetUrl = sheetPost.getSheet().getSheetUrl();
        if (sheetUrl == null) {
            return;
        }

        String fileName = extractFileNameFromUrl(sheetUrl);
        if (fileName == null) {
            log.warn("Could not extract filename from URL: {}", sheetUrl);
            return;
        }

        try {
            Path pdfPath = Paths.get(basePath, "sheets", fileName);
            Files.deleteIfExists(pdfPath);
            log.debug("delete pdf : {}", pdfPath);
        } catch (IOException e) {
            log.error("Failed to delete PDF file: {}", fileName, e);
        }

        for (int i = 0; i < sheetPost.getSheet().getPageNum(); i++) {
            try {
                String thumbnailName = fileName.split("\\.")[0] + "-" + i + ".jpg";
                Path thumbnailPath = Paths.get(basePath, "thumbnails", thumbnailName);
                Files.deleteIfExists(thumbnailPath);
                log.debug("delete thumbnail : {}", thumbnailPath);
            } catch (IOException e) {
                log.error("Failed to delete thumbnail file: {}-{}", fileName, i, e);
            }
        }
    }

    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }

        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }

        return null;
    }

    @Override
    public URL createFileUrl(String sheetUrl) {
        try {
            String fileName = extractFileNameFromUrl(sheetUrl);
            if (fileName == null) {
                throw new RuntimeException("Failed to extract filename from URL: " + sheetUrl);
            }

            return new URL("http://localhost:8080/api/v1/files/sheets/" + fileName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create file URL", e);
        }
    }

    @Override
    public String buildSheetUrl(String filename) {
        return "http://localhost:8080/api/v1/files/sheets/" + filename;
    }

    @Override
    public String buildThumbnailUrls(String filename, int pageNum) {
        StringBuilder thumbnailUrls = new StringBuilder();
        String baseFileName = filename.substring(0, filename.lastIndexOf('.'));

        for (int i = 0; i < pageNum; i++) {
            if (i > 0) {
                thumbnailUrls.append(",");
            }
            thumbnailUrls.append("http://localhost:8080/api/v1/files/thumbnails/")
                    .append(baseFileName)
                    .append("-")
                    .append(i)
                    .append(".jpg");
        }

        return thumbnailUrls.toString();
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId) {
        try {
            log.info("upload sheet async start - local storage, uploadId: {}", uploadId);
            uploadSheet(file, filename, metadata);
            log.info("upload sheet async end - local storage, uploadId: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to upload sheet async for uploadId: {}", uploadId, e);
        }
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId) {
        try {
            log.info("upload thumbnail async start - local storage, uploadId: {}", uploadId);
            uploadThumbnail(document, filename, metadata);
            log.info("upload thumbnail async end - local storage, uploadId: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to upload thumbnail async for uploadId: {}", uploadId, e);
        }
    }
}
