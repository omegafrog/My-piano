package com.omegafrog.My.piano.app.web.event;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.service.FileUploadService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
public class AfterSheetPostCreatedHandler {

  private final KafkaTemplate kafkaTemplate;
  private final FileUploadService fileUploadService;
  private final SheetPostRepository sheetPostRepository;

  public AfterSheetPostCreatedHandler(KafkaTemplate kafkaTemplate, FileUploadService fileUploadService,
      SheetPostRepository sheetPostRepository) {
    this.kafkaTemplate = kafkaTemplate;
    this.fileUploadService = fileUploadService;
    this.sheetPostRepository = sheetPostRepository;
  }

  @TransactionalEventListener
  public void produceEvent(SheetPostCreatedEvent event) {
    Long sheetPostId = event.getSheetPostId();
    String uploadId = event.getUploadId();
    Map<String, String> uploadData = fileUploadService.getUploadData(uploadId);
    SheetPost sheetPost = sheetPostRepository.findById(sheetPostId)
        .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity. id:" + sheetPostId));

    // Redis에서 originalFileName 조회
    String originalFileName = uploadData.get("originalFileName");
    // Redis에 uploadId:sheetPostId 매핑 저장
    fileUploadService.updateUploadMapping(uploadId, sheetPostId);

    // 업로드가 이미 완료된 경우 즉시 URL 업데이트
    if (fileUploadService.isUploadCompleted(uploadId)) {
      log.debug("파일 업로드 완료 이후 sheetPost 업데이트 중.");
      String sheetUrl = uploadData.get("sheetUrl");
      String thumbnailUrl = uploadData.get("thumbnailUrl");
      String pageNumStr = uploadData.get("pageNum");

      log.debug("{},{},{}", sheetUrl, thumbnailUrl, pageNumStr);

      if (sheetUrl != null && !sheetUrl.isEmpty() &&
          thumbnailUrl != null && !thumbnailUrl.isEmpty() &&
          pageNumStr != null && !pageNumStr.isEmpty()) {

        int pageNum = Integer.parseInt(pageNumStr);
        sheetPost.getSheet().updateUrls(sheetUrl, thumbnailUrl);
        sheetPost.getSheet().updatePageNum(pageNum);
        sheetPost.getSheet().updateOriginalFileName(originalFileName);
        log.debug(
            "Immediately updated URLs for SheetPost: uploadId={}, sheetPostId={}, sheetUrl={}, thumbnailUrl={}, pageNum={}",
            uploadId, sheetPost.getId(), sheetUrl, thumbnailUrl, pageNum);
      }
    }
    kafkaTemplate.send("sheet-post-created-topic", event);
  }
}
