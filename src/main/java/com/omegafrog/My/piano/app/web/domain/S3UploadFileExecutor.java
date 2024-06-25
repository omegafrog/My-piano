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
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class S3UploadFileExecutor {

    private final S3Template s3Template;
    private final S3Client s3Client;
    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Async("ThreadPoolTaskExecutor")
    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload sheet start");
        FileInputStream inputStream = new FileInputStream(file);
        s3Template.upload(bucketName, filename, inputStream, metadata);
        log.info("upload sheet end");
    }

    @Async("ThreadPoolTaskExecutor")
    public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) throws FileNotFoundException {
        log.info("upload thumbnail start");
        List<File> files = generateThumbnail(document, filename);
        for (int i = 0; i < files.size(); i++) {
            FileInputStream inputStream = new FileInputStream(files.get(i));
            String key = filename.substring(0, filename.lastIndexOf('.')) + "-" + i + ".jpg";
            s3Template.upload(bucketName,
                    key,
                    inputStream, metadata);
            s3Client.putObjectAcl(PutObjectAclRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ).build());
        }
        log.info("upload thumbnail end");
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
    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload profile start");
        try {
            // 임시 파일을 만들어 저장
            File temp = File.createTempFile("temp", ".data");
            temp.deleteOnExit();
            ReadableByteChannel src = profileImg.getResource().readableChannel();
            FileChannel dest = new FileOutputStream(temp).getChannel();
            dest.transferFrom(src, 0, profileImg.getSize());

            FileInputStream inputStream = new FileInputStream(temp);
            s3Template.upload(bucketName, filename, inputStream, metadata);
            s3Client.putObjectAcl(PutObjectAclRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .acl(ObjectCannedACL.PUBLIC_READ).build());
        }catch (FileNotFoundException ex){
            log.error("프로필 올리기 실패",ex);
        }
        log.info("upload profile end");
    }
    @Async("ThreadPoolTaskExecutor")
    public void removeProfileImg(String url){
        log.info("remove profile start");
        String s3Url = new StringBuilder().append("s3://").append(bucketName).append("/").append(url).toString();

        s3Template.deleteObject(s3Url);
        log.info("remove profile end");

    }

    @Async("ThreadPoolTaskExecutor")
    public void removeSheetPost(SheetPost sheetPost) {
        String fileName = sheetPost.getSheet().getSheetUrl();
        String pdfUrl = new StringBuilder("s3://").append(bucketName).append("/").append(fileName).toString();
        List<String> thumbnailUrls = new ArrayList<>();
        for (int i = 0; i < sheetPost.getSheet().getPageNum(); i++) {
            thumbnailUrls.add(
                    new StringBuilder("s3://").append(bucketName).append("/").append(
                                    fileName.split("\\.")[0]
                            ).append("-")
                            .append(i)
                            .append(".jpg")
                            .toString()
            );
        }
        log.debug("delete pdf : {}", pdfUrl);
        s3Template.deleteObject(pdfUrl);
        thumbnailUrls.forEach(url -> {
            log.debug("delete thumbnail : {}", thumbnailUrls);
            s3Template.deleteObject(url);
        });
    }

    public URL createFileUrl(String sheetUrl) {
        return s3Template.createSignedGetURL(bucketName, sheetUrl, Duration.ofHours(24));
    }
}
