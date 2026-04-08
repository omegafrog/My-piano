package com.omegafrog.My.piano.app.web.domain;

import com.omegafrog.My.piano.app.web.exception.CreateThumbnailFailedException;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class S3UploadFileExecutor implements UploadFileExecutor {

    private final S3Template s3Template;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Override
    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload sheet start");
        try (FileInputStream inputStream = new FileInputStream(file)) {
            s3Template.upload(bucketName, filename, inputStream, metadata);
        }
        log.info("upload sheet end");
    }

    @Override
    public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata)
            throws FileNotFoundException {
        log.info("upload thumbnail start");
        List<BufferedImage> images = generateThumbnail(document, filename);
        for (int i = 0; i < images.size(); i++) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                String key = filename.substring(0, filename.lastIndexOf('.')) + "-" + i + ".jpg";
                ImageIO.write(images.get(i), "jpg", outputStream);
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                    s3Template.upload(bucketName, key, inputStream, metadata);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("upload thumbnail end");
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

            BufferedImage blurredImage = blurImage(
                    firstImg.getSubimage(0, visibleHeight, width, height - visibleHeight));

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

    private static BufferedImage blurImage(BufferedImage image) {
        float[] matrix = new float[225];
        for (int i = 0; i < 225; ++i) {
            matrix[i] = 1.0f / 225.f;
        }

        Kernel kernel = new Kernel(15, 15, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata)
            throws IOException {
        log.info("upload profile start");
        try {
            try (InputStream inputStream = profileImg.getInputStream()) {
                s3Template.upload(bucketName, filename, inputStream, metadata);
            }
        } catch (FileNotFoundException ex) {
            log.error("프로필 올리기 실패", ex);
        }
        log.info("upload profile end");
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void removeProfileImg(String url) {
        log.info("remove profile start");
        String s3Url = new StringBuilder().append("s3://").append(bucketName).append("/").append(url).toString();

        s3Template.deleteObject(s3Url);
        log.info("remove profile end");
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
            log.warn("Could not extract filename from S3 URL: {}", sheetUrl);
            return;
        }

        String pdfUrl = new StringBuilder("s3://").append(bucketName).append("/").append(fileName).toString();
        List<String> thumbnailUrls = new ArrayList<>();
        for (int i = 0; i < sheetPost.getSheet().getPageNum(); i++) {
            thumbnailUrls.add(
                    new StringBuilder("s3://").append(bucketName).append("/").append(
                            fileName.split("\\.")[0]).append("-")
                            .append(i)
                            .append(".jpg")
                            .toString());
        }
        log.debug("delete pdf : {}", pdfUrl);
        s3Template.deleteObject(pdfUrl);
        thumbnailUrls.forEach(url -> {
            log.debug("delete thumbnail : {}", url);
            s3Template.deleteObject(url);
        });
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
        String fileName = extractFileNameFromUrl(sheetUrl);
        if (fileName == null) {
            throw new RuntimeException("Failed to extract filename from URL: " + sheetUrl);
        }
        return s3Template.createSignedGetURL(bucketName, fileName, Duration.ofHours(24));
    }

    @Override
    public String buildSheetUrl(String filename) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + filename;
    }

    @Override
    public String buildThumbnailUrls(String filename, int pageNum) {
        StringBuilder thumbnailUrls = new StringBuilder();
        String baseFileName = filename.substring(0, filename.lastIndexOf('.'));

        for (int i = 0; i < pageNum; i++) {
            if (i > 0) {
                thumbnailUrls.append(",");
            }
            thumbnailUrls.append("https://")
                    .append(bucketName)
                    .append(".s3.")
                    .append(region)
                    .append(".amazonaws.com/")
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
            log.info("upload sheet async start - S3, uploadId: {}", uploadId);
            uploadSheet(file, filename, metadata);
            log.info("upload sheet async end - S3, uploadId: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to upload sheet async to S3 for uploadId: {}", uploadId, e);
        }
    }

    @Override
    @Async("ThreadPoolTaskExecutor")
    public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId) {
        try {
            log.info("upload thumbnail async start - S3, uploadId: {}", uploadId);
            uploadThumbnail(document, filename, metadata);
            log.info("upload thumbnail async end - S3, uploadId: {}", uploadId);
        } catch (Exception e) {
            log.error("Failed to upload thumbnail async to S3 for uploadId: {}", uploadId, e);
        }
    }
}
