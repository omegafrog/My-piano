package com.omegafrog.My.piano.app.web.domain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.event.FileUploadCompletedEvent;
import com.omegafrog.My.piano.app.web.event.FileUploadFailedEvent;
import com.omegafrog.My.piano.app.web.exception.CreateThumbnailFailedException;
import com.omegafrog.My.piano.app.web.service.outbox.UploadOutboxService;

import io.awspring.cloud.s3.ObjectMetadata;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFileStorageExecutor implements UploadFileExecutor {

	@Value("${local.storage.base-path}")
	private String basePath;

	@Autowired
	private UploadOutboxService uploadOutboxService;

	@Async("ThreadPoolTaskExecutor")
	public void uploadSheet(File file, String filename, ObjectMetadata metadata) {
		log.info("upload sheet start - local storage");
		try {
			Path targetPath = Paths.get(basePath, "sheets", filename);
			Files.createDirectories(targetPath.getParent());
			Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("upload sheet end - local storage");
	}

	@Async("ThreadPoolTaskExecutor")
	public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) {
		try {
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
			BufferedImage blurredImage = blurImage(
					firstImg.getSubimage(0, visibleHeight, width, height - visibleHeight));

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

	private float[] matrix = new float[225];
	private ConvolveOp op = null;

	@PostConstruct
	public void init() {
		for (int i = 0; i < 225; ++i)
			matrix[i] = 1.0f / 225.f;
		op = new ConvolveOp(new Kernel(15, 15, matrix), ConvolveOp.EDGE_NO_OP, null);
	}

	private BufferedImage blurImage(BufferedImage image) {
		return op.filter(image, null);
	}

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

	@Async("ThreadPoolTaskExecutor")
	public void removeSheetPost(SheetPost sheetPost) {
		String sheetUrl = sheetPost.getSheet().getSheetUrl();
		if (sheetUrl == null)
			return;

		// URL에서 파일명만 추출 (예: "http://localhost:8080/sheets/filename.pdf" ->
		// "filename.pdf")
		String fileName = extractFileNameFromUrl(sheetUrl);
		if (fileName == null) {
			log.warn("Could not extract filename from URL: {}", sheetUrl);
			return;
		}

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

	/**
	 * URL에서 파일명만 추출하는 헬퍼 메서드
	 * 
	 * @param url 전체 URL (예: "http://localhost:8080/sheets/filename.pdf")
	 * @return 파일명 (예: "filename.pdf") 또는 null (추출 실패 시)
	 */
	private String extractFileNameFromUrl(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}

		// URL이 http://로 시작하지 않는다면 이미 파일명일 가능성
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return url;
		}

		// URL의 마지막 "/" 이후 부분을 파일명으로 추출
		int lastSlashIndex = url.lastIndexOf("/");
		if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
			return url.substring(lastSlashIndex + 1);
		}

		return null;
	}

	public URL createFileUrl(String sheetUrl) {
		try {
			// URL에서 파일명만 추출
			String fileName = extractFileNameFromUrl(sheetUrl);
			if (fileName == null) {
				throw new RuntimeException("Failed to extract filename from URL: " + sheetUrl);
			}

			// static 리소스로 접근 가능한 URL 생성
			return new URL("http://localhost:8080/sheets/" + fileName);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Failed to create file URL", e);
		}
	}

	@Async("ThreadPoolTaskExecutor")
	public void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId) {
		try {
			log.info("upload sheet async start - local storage, uploadId: {}", uploadId);
			Path targetPath = Paths.get(basePath, "sheets", filename);
			Files.createDirectories(targetPath.getParent());
			Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			log.info("upload sheet async end - local storage, uploadId: {}", uploadId);

			// 전체 URL 생성 (프로토콜과 도메인 포함)
			String fullSheetUrl = "http://localhost:8080/sheets/" + filename;

			// 성공 이벤트 발행
			FileUploadCompletedEvent event = FileUploadCompletedEvent.create(
					uploadId, fullSheetUrl, null, filename, 0);
			uploadOutboxService.enqueueCompleted(event);

		} catch (Exception e) {
			log.error("Failed to upload sheet async for uploadId: {}", uploadId, e);

			// 실패 이벤트 발행
			FileUploadFailedEvent failedEvent = FileUploadFailedEvent.create(
					uploadId, filename, e.getMessage(), "SHEET_UPLOAD_FAILED");
			uploadOutboxService.enqueueFailed(failedEvent);
		}
	}

	@Async("ThreadPoolTaskExecutor")
	public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId) {
		try {
			log.info("upload thumbnail async start - local storage, uploadId: {}", uploadId);
			List<File> files = generateThumbnail(document, filename);
			Path thumbnailDir = Paths.get(basePath, "thumbnails");
			Files.createDirectories(thumbnailDir);

			StringBuilder thumbnailUrls = new StringBuilder();
			for (int i = 0; i < files.size(); i++) {
				String thumbnailFilename = filename.substring(0, filename.lastIndexOf('.')) + "-" + i + ".jpg";
				Path targetPath = thumbnailDir.resolve(thumbnailFilename);
				Files.copy(files.get(i).toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

				// 전체 URL 생성 (프로토콜과 도메인 포함)
				String fullThumbnailUrl = "http://localhost:8080/thumbnails/" + thumbnailFilename;

				if (i > 0)
					thumbnailUrls.append(",");
				thumbnailUrls.append(fullThumbnailUrl);
			}
			log.info("upload thumbnail async end - local storage, uploadId: {}", uploadId);

			// 성공 이벤트 발행 (썸네일만)
			FileUploadCompletedEvent event = FileUploadCompletedEvent.create(
					uploadId, null, thumbnailUrls.toString(), filename, files.size());
			uploadOutboxService.enqueueCompleted(event);

		} catch (Exception e) {
			log.error("Failed to upload thumbnail async for uploadId: {}", uploadId, e);

			// 실패 이벤트 발행
			FileUploadFailedEvent failedEvent = FileUploadFailedEvent.create(
					uploadId, filename, e.getMessage(), "THUMBNAIL_UPLOAD_FAILED");
			uploadOutboxService.enqueueFailed(failedEvent);
		}
	}

}
