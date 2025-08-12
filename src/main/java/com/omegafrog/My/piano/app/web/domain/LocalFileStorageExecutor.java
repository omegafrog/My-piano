package com.omegafrog.My.piano.app.web.domain;

import com.omegafrog.My.piano.app.web.exception.CreateThumbnailFailedException;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalFileStorageExecutor {

    @Value("${local.storage.base-path}")
    private String basePath;

    @Async("ThreadPoolTaskExecutor")
    public void uploadSheet(File file, String filename) throws IOException {
        log.info("upload sheet start - local storage");
        Path targetPath = Paths.get(basePath, "sheets", filename);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("upload sheet end - local storage");
    }

    @Async("ThreadPoolTaskExecutor")
    public void uploadThumbnail(PDDocument document, String filename) throws IOException {
        log.info("upload thumbnail start - local storage");
        List<File> files = generateThumbnail(document, filename);
        Path thumbnailDir = Paths.get(basePath, "thumbnails");
        Files.createDirectories(thumbnailDir);
        
        for (int i = 0; i < files.size(); i++) {
            String thumbnailFilename = filename.substring(0, filename.lastIndexOf('.')) + "-" + i + ".jpg";
            Path targetPath = thumbnailDir.resolve(thumbnailFilename);
            Files.copy(files.get(i).toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("upload thumbnail end - local storage");
    }

    private List<File> generateThumbnail(PDDocument document, String pdfFilePath) {
        try {
            // PDF를 이미지로 변환
            int pages = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);
            List<File> thumbnailsFiles = new ArrayList<>();
            int dpi = 72; // 변경할 해상도 (dpi)

            // 첫 페이지 렌더링
            BufferedImage firstImg = renderer.renderImageWithDPI(0, dpi);
            int width = firstImg.getWidth();
            int height = firstImg.getHeight();

            // 이미지의 40%만 보이도록 자르기
            int visibleHeight = (int) (height * 0.4);
            BufferedImage visibleImage = firstImg.getSubimage(0, 0, width, visibleHeight);

            // 나머지 부분에 블러 처리 적용
            BufferedImage blurredImage = blurImage(firstImg.getSubimage(0, visibleHeight, width, height - visibleHeight));

            BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = combinedImage.createGraphics();
            g.drawImage(visibleImage, 0, 0, null);
            g.drawImage(blurredImage, 0, visibleHeight, null);
            g.dispose();

            String firstThumbnailPath = pdfFilePath.substring(0, pdfFilePath.lastIndexOf('.')) + "-" + 0 + ".jpg";
            log.info("이미지 파일 생성: {}", firstThumbnailPath);
            File firstThumbnailFile = File.createTempFile(firstThumbnailPath, null);
            firstThumbnailFile.deleteOnExit();
            log.info("이미지 파일에 이미지 쓰기");
            ImageIO.write(combinedImage, "jpg", firstThumbnailFile);
            thumbnailsFiles.add(firstThumbnailFile);

            // 두번째 페이지부터 렌더링
            for (int i = 1; i < pages; i++) {
                log.info("이미지로  변환 시작: {}-{}", pdfFilePath, i);

                BufferedImage img = renderer.renderImageWithDPI(i, dpi);

                String thumbnailPath = pdfFilePath.substring(0, pdfFilePath.lastIndexOf('.')) + "-" + i + ".jpg";
                log.info("이미지 파일 생성: {}", thumbnailPath);
                File thumbnailFile = File.createTempFile(thumbnailPath, null);
                thumbnailFile.deleteOnExit();

                blurredImage = blurImage(img);

                log.info("이미지 파일에 이미지 쓰기");
                ImageIO.write(blurredImage, "jpg", thumbnailFile);
                thumbnailsFiles.add(thumbnailFile);
            }
            document.close();

            return thumbnailsFiles;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CreateThumbnailFailedException(e);
        }
    }

    private static BufferedImage blurImage(BufferedImage image){
        float[] matrix = new float[225];
        for(int i =0 ;i < 225; ++i)
            matrix[i] = 1.0f/225.f;

        Kernel kernel = new Kernel(15,15, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    @Async("ThreadPoolTaskExecutor")
    public void uploadProfileImg(MultipartFile profileImg, String filename) throws IOException {
        log.info("upload profile start - local storage");
        Path profileDir = Paths.get(basePath, "profiles");
        Files.createDirectories(profileDir);
        Path targetPath = profileDir.resolve(filename);
        
        try (InputStream inputStream = profileImg.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("upload profile end - local storage");
    }

    @Async("ThreadPoolTaskExecutor")
    public void removeProfileImg(String filename){
        log.info("remove profile start - local storage");
        try {
            Path filePath = Paths.get(basePath, "profiles", filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete profile image: {}", filename, e);
        }
        log.info("remove profile end - local storage");
    }

    @Async("ThreadPoolTaskExecutor")
    public void removeSheetPost(SheetPost sheetPost) {
        String fileName = sheetPost.getSheet().getSheetUrl();
        
        // Delete PDF file
        try {
            Path pdfPath = Paths.get(basePath, "sheets", fileName);
            Files.deleteIfExists(pdfPath);
            log.debug("delete pdf : {}", pdfPath);
        } catch (IOException e) {
            log.error("Failed to delete PDF file: {}", fileName, e);
        }
        
        // Delete thumbnail files
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

    public URL createFileUrl(String sheetUrl) {
        try {
            Path filePath = Paths.get(basePath, "sheets", sheetUrl);
            return filePath.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create file URL", e);
        }
    }
}