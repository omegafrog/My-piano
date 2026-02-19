package com.omegafrog.My.piano.app.web.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.omegafrog.My.piano.app.web.service.FileUploadLinkService;

@Component
@Transactional
@Slf4j
public class AfterSheetPostCreatedHandler {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final FileUploadLinkService fileUploadLinkService;

	public AfterSheetPostCreatedHandler(ApplicationEventPublisher applicationEventPublisher,
			FileUploadLinkService fileUploadLinkService) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.fileUploadLinkService = fileUploadLinkService;
	}

  @TransactionalEventListener
  public void produceEvent(SheetPostCreatedEvent event) {
    Long sheetPostId = event.getSheetPostId();
    String uploadId = event.getUploadId();

		fileUploadLinkService.linkUploadToSheetPost(uploadId, sheetPostId);
		applicationEventPublisher.publishEvent(new SheetPostCreatedAfterCommitEvent(uploadId, sheetPostId));
	}
}
