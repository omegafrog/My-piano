package com.omegafrog.My.piano.app.web.service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.event.SheetPostCreatedEvent;
import com.omegafrog.My.piano.app.web.event.SheetPostSearchedEvent;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SheetPostApplicationService {
	private final SheetPostIndexRepository sheetPostIndexRepository;

	private final SheetPostRepository sheetPostRepository;
	private final UserRepository userRepository;

	private final ElasticSearchInstance elasticSearchInstance;
	private final FileStorageExecutor uploadFileExecutor;
	private final SheetPostViewCountRepository sheetPostViewCountRepository;
	private final AuthenticationUtil authenticationUtil;
	private final FileUploadService fileUploadService;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	@Qualifier("SheetPostLikeCountRepository")
	private LikeCountRepository likeCountRepository;

	public SheetPostDto getSheetPost(Long id) {
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id));
		int incrementedViewCount = sheetPostViewCountRepository.incrementViewCount(sheetPost);
		int likeCount = likeCountRepository.findById(sheetPost.getId()).getLikeCount();
		SheetPostDto dto = sheetPost.toDto();
		dto.setLikeCount(likeCount);
		dto.setViewCount(incrementedViewCount);
		return dto;
	}

	public SheetPostDto writeSheetPost(RegisterSheetPostDto dto) throws IOException {
		User loggedInUser = authenticationUtil.getLoggedInUser();

		// uploadId 검증
		String uploadId = dto.getUploadId();
		if (uploadId == null || uploadId.trim().isEmpty()) {
			throw new IllegalArgumentException("uploadId는 필수입니다.");
		}

		try {

			// Sheet 엔티티 생성 (초기에는 URL이 없음, originalFileName 설정)
			Sheet sheet = dto.getSheet().createEntity(loggedInUser, 0); // pageNum은 나중에 업데이트

			// Sheet 엔티티에 원본 파일명 설정
			sheet = Sheet.builder()
					.title(sheet.getTitle())
					.pageNum(sheet.getPageNum())
					.difficulty(sheet.getDifficulty())
					.instrument(sheet.getInstrument())
					.genres(sheet.getGenres())
					.isSolo(sheet.isSolo())
					.lyrics(sheet.isLyrics())
					.sheetUrl(sheet.getSheetUrl())
					.thumbnailUrl(sheet.getThumbnailUrl())
					.user(loggedInUser)
					.build();
			SheetPost sheetPost = SheetPost.builder()
					.title(dto.getTitle())
					.sheet(sheet)
					.artist(loggedInUser)
					.price(dto.getPrice())
					.content(dto.getContent())
					.build();
			SheetPost saved = sheetPostRepository.save(sheetPost);

			eventPublisher.publishEvent(new SheetPostCreatedEvent(uploadId, sheetPost.getId()));

			log.info("SheetPost created with uploadId: {}, sheetPostId: {}", uploadId, saved.getId());
			return saved.toDto();

		} catch (Exception e) {
			log.error("Failed to create SheetPost with uploadId: {}", uploadId, e);
			throw e;
		}
	}

	public SheetPostDto update(Long id, UpdateSheetPostDto dto) throws IOException {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
		log.debug("updateDto : {}", dto);

		if (!sheetPost.getAuthor().equals(loggedInUser)) {
			throw new AccessDeniedException("Cannot update other user's sheet post." + id);
		}

		// uploadId가 있다면 새로운 파일이 업로드된 것으로 처리
		if (dto.getUploadId() != null && !dto.getUploadId().trim().isEmpty()) {
			String uploadId = dto.getUploadId();

			// Redis Hash에서 업로드 데이터 조회
			Map<String, String> uploadData = fileUploadService.getUploadData(uploadId);
			if (uploadData == null) {
				throw new IllegalArgumentException("유효하지 않은 uploadId입니다: " + uploadId);
			}

			// 기존 파일 삭제
			uploadFileExecutor.removeSheetPost(sheetPost);

			// Redis에 uploadId:sheetPostId 매핑 저장 (기존 SheetPost에 새 파일 연결)
			fileUploadService.updateUploadMapping(uploadId, sheetPost.getId());

			// 업로드가 이미 완료된 경우 즉시 URL 업데이트
			if (fileUploadService.isUploadCompleted(uploadId)) {
				String sheetUrl = uploadData.get("sheetUrl");
				String thumbnailUrl = uploadData.get("thumbnailUrl");
				String pageNumStr = uploadData.get("pageNum");
				String originalFileName = uploadData.get("originalFileName");

				if (sheetUrl != null && !sheetUrl.isEmpty() &&
						thumbnailUrl != null && !thumbnailUrl.isEmpty() &&
						pageNumStr != null && !pageNumStr.isEmpty()) {

					dto.getSheet().setSheetUrl(sheetUrl);
					dto.getSheet().setThumbnailUrl(thumbnailUrl);
					dto.getSheet().setPageNum(Integer.parseInt(pageNumStr));
					dto.getSheet().setOriginalFileName(originalFileName);
					Sheet sheet = sheetPost.getSheet();
					int pageNum = Integer.parseInt(pageNumStr);

					// Sheet URL, 썸네일, 페이지 수 업데이트
					sheet.updateUrls(sheetUrl, thumbnailUrl);
					sheet.updatePageNum(pageNum);

					// originalFileName 업데이트
					if (originalFileName != null && !originalFileName.isEmpty()) {
						sheet.updateOriginalFileName(originalFileName);
					}

					// updateDto의 sheet에도 pageNum 설정 (기존 로직 호환성)
					if (dto.getSheet() != null) {
						dto.getSheet().setPageNum(pageNum);
					}
					sheetPost.updateSheet(sheet);

					log.info(
							"Immediately updated URLs for existing SheetPost: uploadId={}, sheetPostId={}, sheetUrl={}, thumbnailUrl={}, pageNum={}",
							uploadId, sheetPost.getId(), sheetPost.getSheet().getSheetUrl(),
							sheetPost.getSheet().getThumbnailUrl(), pageNum);
				}
			}
		}

		// SheetPost 업데이트 (기본 정보: title, content, price 등)
		SheetPost updated = sheetPost.update(dto);
		elasticSearchInstance.invertIndexingSheetPost(updated);
		return updated.toDto();
	}

	public String getSheetUrl(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find Sheetpost entity."));
		User user = userRepository.findById(loggedInUser.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cannot find User entity."));
		if (!user.isPurchased(sheetPost))
			throw new BadCredentialsException("구매하지 않은 sheet post 를 조회할 수 없습니다.");
		URL fileUrl = uploadFileExecutor.createFileUrl(sheetPost.getSheet().getSheetUrl());
		return fileUrl.toString();
	}

	public void delete(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
		uploadFileExecutor.removeSheetPost(sheetPost);
		sheetPostIndexRepository.deleteById(id);
		if (sheetPost.getAuthor().equals(loggedInUser))
			sheetPostRepository.deleteById(sheetPost.getId());
		else
			throw new AccessDeniedException("Cannot delete other user's sheet post entity : " + id);
	}

	public void likePost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity:" + id));
		loggedInUser.likeSheetPost(sheetPost);
		likeCountRepository.incrementLikeCount(sheetPost);
	}

	public boolean isLikedSheetPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost targetSheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(("Cannot find SheetPost entity:" + id)));
		return loggedInUser.getLikedSheetPosts()
				.stream().anyMatch(likedSheetPost -> likedSheetPost.getSheetPost().equals(targetSheetPost));
	}

	public void dislikeSheetPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
		boolean removed = loggedInUser.getLikedSheetPosts().removeIf(item -> item.getSheetPost().getId().equals(id));
		if (!removed)
			throw new EntityNotFoundException("좋아요를 누르지 않았습니다." + id);
		likeCountRepository.decrementLikeCount(sheetPost);
	}

	public void scrapSheetPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity:" + id));
		User user = userRepository.findById(loggedInUser.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
		user.scrapSheetPost(sheetPost);
	}

	public boolean isScrappedSheetPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost targetSheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
		User user = userRepository.findById(loggedInUser.getId())
				.orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
		return user.getScrappedSheetPosts().stream().anyMatch(item -> item.getSheetPost().equals(targetSheetPost));
	}

	public void unScrapSheetPost(Long id) {
		User loggedInUser = authenticationUtil.getLoggedInUser();
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));

		loggedInUser.unScrapSheetPost(sheetPost);
	}

	public Page<SheetPostListDto> getSheetPosts(
			String searchSentence,
			List<String> instrument,
			List<String> difficulty,
			List<String> genre,
			Pageable pageable) throws IOException {

		Page<Long> sheetPostIds = elasticSearchInstance.searchSheetPost(
				searchSentence, instrument, difficulty, genre, pageable);
		List<SheetPostListDto> res = sheetPostRepository.findByIds(sheetPostIds.getContent(), pageable);
		Map<Long, Integer> viewCountsBySheetPostIds = sheetPostViewCountRepository.getViewCountsByIds(
				sheetPostIds.getContent());
		List<SheetPostListDto> sheetPostLists = res.stream().map(sheetPostListDto -> {
			sheetPostListDto.updateViewCount(
					viewCountsBySheetPostIds.get(sheetPostListDto.getId()));
			return sheetPostListDto;
		}).toList();
		if (searchSentence != null && instrument != null && difficulty != null && genre != null) {
			eventPublisher.publishEvent(new SheetPostSearchedEvent(searchSentence, instrument, difficulty, genre));
		}
		return PageableExecutionUtils.getPage(sheetPostLists, pageable, sheetPostIds::getTotalElements);
	}

}
