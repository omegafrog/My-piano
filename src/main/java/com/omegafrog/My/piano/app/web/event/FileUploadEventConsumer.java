package com.omegafrog.My.piano.app.web.event;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.service.FileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadEventConsumer {

	private final FileUploadService fileUploadService;
	private final SheetPostRepository sheetPostRepository;

	@KafkaListener(topics = "file-upload-completed-topic", groupId = "mypiano-consumer-group")
	public void handleFileUploadCompleted(FileUploadCompletedEvent event) {
		log.info("Received file upload completed event: {}", event.getEventId());

		try {
			// Redis Hash 업데이트 (이벤트에서 받은 데이터로)
			fileUploadService.updateUploadData(
					event.getUploadId(),
					event.getSheetUrl(),
					event.getThumbnailUrl(),
					event.getPageNum());
			// if (event.getSheetUrl() != null && !event.getSheetUrl().isEmpty()) {

			// }

			// if (event.getThumbnailUrl() != null && !event.getThumbnailUrl().isEmpty()) {
			// fileUploadService.updateUploadData(
			// event.getUploadId(),
			// null,
			// event.getThumbnailUrl(),
			// event.getPageNum()
			// );
			// }

			// Redis Hash에서 전체 업로드 데이터 조회
			Map<String, String> uploadData = fileUploadService.getUploadData(event.getUploadId());

			if (uploadData == null) {
				log.warn("No upload data found for uploadId: {}", event.getUploadId());
				return;
			}

			String sheetPostIdStr = uploadData.get("sheetPostId");
			if (sheetPostIdStr == null || sheetPostIdStr.isEmpty()) {
				log.warn("No sheetPostId found for uploadId: {}", event.getUploadId());
				return;
			}

			// sheetPostId가 숫자인지 확인
			Long sheetPostId;
			try {
				sheetPostId = Long.parseLong(sheetPostIdStr);
			} catch (NumberFormatException e) {
				log.warn("SheetPostId is not a number for uploadId: {}, value: {}", event.getUploadId(),
						sheetPostIdStr);
				return;
			}

			// SheetPost 조회 및 업데이트
			SheetPost sheetPost = sheetPostRepository.findById(sheetPostId)
					.orElseThrow(() -> new RuntimeException("SheetPost not found: " + sheetPostId));

			Sheet sheet = sheetPost.getSheet();

			// Redis Hash에서 URL 데이터 가져오기
			String sheetUrl = uploadData.get("sheetUrl");
			String thumbnailUrl = uploadData.get("thumbnailUrl");
			String pageNumStr = uploadData.get("pageNum");

			// Sheet 엔티티의 URL 필드 업데이트
			if (sheetUrl != null && !sheetUrl.isEmpty()) {
				sheet.updateUrls(sheetUrl, thumbnailUrl);

				// pageNum 업데이트
				if (pageNumStr != null && !pageNumStr.isEmpty()) {
					try {
						int pageNum = Integer.parseInt(pageNumStr);
						sheet.updatePageNum(pageNum);
					} catch (NumberFormatException e) {
						log.warn("Invalid pageNum for uploadId: {}, value: {}", event.getUploadId(), pageNumStr);
					}
				}

				// originalFileName이 없는 경우 Redis에서 가져와서 설정
				String originalFileName = uploadData.get("originalFileName");
				if (originalFileName != null && !originalFileName.isEmpty()) {
					sheet.updateOriginalFileName(originalFileName);
				}

				// 저장
				sheetPostRepository.save(sheetPost);

				log.info(
						"Successfully async updated SheetPost URLs for uploadId: {}, sheetPostId: {}, sheetUrl: {}, thumbnailUrl: {}, pageNum: {}, originalFileName: {}",
						event.getUploadId(), sheetPostId, sheetUrl, thumbnailUrl, pageNumStr,
						uploadData.get("originalFileName"));
			} else {
				log.warn("No valid URLs found in upload data for uploadId: {}", event.getUploadId());
			}

		} catch (Exception e) {
			log.error("Failed to process file upload completed event for uploadId: {}",
					event.getUploadId(), e);
		}
	}

	@KafkaListener(topics = "file-upload-failed-topic", groupId = "mypiano-consumer-group")
	public void handleFileUploadFailed(FileUploadFailedEvent event) {
		log.warn("Received file upload failed event: {}", event.getEventId());

		try {
			// Redis Hash에서 전체 업로드 데이터 조회
			Map<String, String> uploadData = fileUploadService.getUploadData(event.getUploadId());

			if (uploadData == null) {
				log.warn("No upload data found for failed uploadId: {}", event.getUploadId());
				return;
			}

			String sheetPostIdStr = uploadData.get("sheetPostId");
			if (sheetPostIdStr == null || sheetPostIdStr.isEmpty()) {
				log.warn("No sheetPostId found for failed uploadId: {}", event.getUploadId());
				return;
			}

			// sheetPostId가 숫자인지 확인
			Long sheetPostId;
			try {
				sheetPostId = Long.parseLong(sheetPostIdStr);
			} catch (NumberFormatException e) {
				log.warn("SheetPostId is not a number for failed uploadId: {}, value: {}",
						event.getUploadId(), sheetPostIdStr);
				return;
			}

			// TODO: SheetPost 상태를 FAILED로 업데이트하거나 알림 처리
			log.error("File upload failed for uploadId: {}, sheetPostId: {}, reason: {}, errorMessage: {}",
					event.getUploadId(), sheetPostId, event.getFailureReason(), event.getErrorMessage());

		} catch (Exception e) {
			log.error("Failed to process file upload failed event for uploadId: {}",
					event.getUploadId(), e);
		}
	}
}