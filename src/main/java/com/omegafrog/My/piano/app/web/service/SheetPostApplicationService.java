package com.omegafrog.My.piano.app.web.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
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
import com.omegafrog.My.piano.app.web.dto.sheetPost.ArtistInfo;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.event.SheetPostSearchedEvent;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostDetailCachePayload;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCacheKey;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCachePayload;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;

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
	private final SheetPostOutboxService sheetPostOutboxService;
	private final SheetPostCacheCoordinator sheetPostCacheCoordinator;

	@Autowired
	@Qualifier("SheetPostLikeCountRepository")
	private LikeCountRepository likeCountRepository;

	public SheetPostDto getSheetPost(Long id) {
		SheetPostDetailCachePayload payload = getSheetPostDetailWithSWR(id);
		SheetPostDto dto = copySheetPostDto(payload.baseDto());
		int incrementedViewCount = sheetPostViewCountRepository.incrementViewCount(id, payload.initialViewCount());
		int likeCount = likeCountRepository.findById(id).getLikeCount();
		dto.setLikeCount(likeCount);
		dto.setViewCount(incrementedViewCount);
		dto.setLikePost(false);
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
			// 업로드 메타데이터 조회 (원본 파일명 등)
			Map<String, String> uploadData = fileUploadService.getUploadData(uploadId);
			String originalFileName = uploadData == null ? null : uploadData.get("originalFileName");

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
					.originalFileName(originalFileName)
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
			fileUploadService.updateUploadMapping(uploadId, saved.getId());
			sheetPostOutboxService.enqueueCreated(saved.getId());
			sheetPostCacheCoordinator.evictSheetPostListCache();

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

			// 업로드와 링크가 모두 완료된 경우에만 URL을 반영한다.
			if (fileUploadService.isUploadCompleted(uploadId) && fileUploadService.isUploadLinked(uploadId)) {
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
		sheetPostCacheCoordinator.evictSheetPostListCache();
		sheetPostCacheCoordinator.evictSheetPostDetailCache(id);
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
		sheetPostCacheCoordinator.evictSheetPostListCache();
		sheetPostCacheCoordinator.evictSheetPostDetailCache(id);
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
			Pageable pageable) {
		SheetPostListCacheKey cacheKey = SheetPostListCacheKey.of(
				searchSentence,
				instrument,
				difficulty,
				genre,
				pageable);

		SheetPostListCachePayload payload;
		if (cacheKey.cacheable()) {
			payload = getSheetPostListWithSWR(cacheKey, searchSentence, instrument, difficulty, genre, pageable);
		} else {
			payload = loadSheetPostListPayload(searchSentence, instrument, difficulty, genre, pageable);
		}

		List<SheetPostListDto> sheetPostLists = copySheetPostListDtos(payload.items());
		Map<Long, Integer> viewCountsBySheetPostIds = sheetPostViewCountRepository.getViewCountsByIds(
				sheetPostLists.stream().map(SheetPostListDto::getId).toList());
		for (SheetPostListDto sheetPostListDto : sheetPostLists) {
			sheetPostListDto.updateViewCount(viewCountsBySheetPostIds.getOrDefault(sheetPostListDto.getId(), 0));
		}

		if (searchSentence != null) {
			eventPublisher.publishEvent(new SheetPostSearchedEvent(
					payload.rawQuery(),
					searchSentence,
					instrument,
					difficulty,
					genre));
		}

		return PageableExecutionUtils.getPage(sheetPostLists, pageable, payload::totalElements);
	}

	@Transactional(readOnly = true)
	public void warmupSheetPostCaches() {
		Cache listCache = sheetPostCacheCoordinator.getRequiredCache(SheetPostCacheCoordinator.SHEET_POST_LIST_CACHE);
		Cache detailCache = sheetPostCacheCoordinator.getRequiredCache(SheetPostCacheCoordinator.SHEET_POST_DETAIL_CACHE);
		Set<Long> detailIdsToWarmup = new LinkedHashSet<>();

		for (int page = 0; page <= SheetPostCacheCoordinator.WARMUP_MAX_LIST_PAGE; page++) {
			Pageable pageable = PageRequest.of(page, SheetPostCacheCoordinator.WARMUP_LIST_PAGE_SIZE);
			SheetPostListCacheKey cacheKey = SheetPostListCacheKey.of(null, null, null, null, pageable);
			if (!cacheKey.cacheable()) {
				continue;
			}
			String cacheEntryKey = cacheKey.asStringKey();
			try {
				SheetPostListCachePayload loaded = loadSheetPostListPayload(null, null, null, null, pageable);
				listCache.put(cacheEntryKey,
						sheetPostCacheCoordinator.buildSwrValue(
								loaded,
								SheetPostCacheCoordinator.LIST_SOFT_TTL_MIN_MS,
								SheetPostCacheCoordinator.LIST_SOFT_TTL_MAX_MS,
								SheetPostCacheCoordinator.LIST_HARD_TTL_MS));
				for (SheetPostListDto item : loaded.items()) {
					if (item.getId() == null) {
						continue;
					}
					detailIdsToWarmup.add(item.getId());
					if (detailIdsToWarmup.size() >= SheetPostCacheCoordinator.WARMUP_MAX_DETAIL_IDS) {
						break;
					}
				}
			} catch (Exception e) {
				log.warn("sheetpost list warm-up failed for key={}", cacheEntryKey, e);
			}
			if (detailIdsToWarmup.size() >= SheetPostCacheCoordinator.WARMUP_MAX_DETAIL_IDS) {
				break;
			}
		}

		for (Long id : detailIdsToWarmup) {
			try {
				SheetPostDetailCachePayload loaded = loadSheetPostDetailPayload(id);
				detailCache.put(id,
						sheetPostCacheCoordinator.buildSwrValue(
								loaded,
								SheetPostCacheCoordinator.DETAIL_SOFT_TTL_MIN_MS,
								SheetPostCacheCoordinator.DETAIL_SOFT_TTL_MAX_MS,
								SheetPostCacheCoordinator.DETAIL_HARD_TTL_MS));
			} catch (Exception e) {
				log.warn("sheetpost detail warm-up failed for id={}", id, e);
			}
		}

		log.info("sheetpost cache warm-up completed. listPages={}, warmedDetails={}",
				SheetPostCacheCoordinator.WARMUP_MAX_LIST_PAGE + 1,
				detailIdsToWarmup.size());
	}

	public List<SheetPostListDto> getSheetPostAutoComplete(String searchSentence, List<String> instrument,
			List<String> difficulty, List<String> genre) {
		List<SheetPostIndex> result = elasticSearchInstance.getSearchSheetPostAutoComplete(searchSentence, instrument,
				difficulty, genre);
		return sheetPostRepository.findAllById(result.stream().map(item -> item.getId()).toList())
				.stream()
				.map(item -> new SheetPostListDto(item.getId(), item.getTitle(), item.getAuthor().getName(),
						item.getAuthor().getProfileSrc(), item.getSheet().getTitle(), item.getSheet().getDifficulty(),
						item.getSheet().getGenres(), item.getSheet().getInstrument(), item.getCreatedAt(), item.getPrice()))
				.toList();
	}

	private SheetPostListCachePayload getSheetPostListWithSWR(
			SheetPostListCacheKey cacheKey,
			String searchSentence,
			List<String> instrument,
			List<String> difficulty,
			List<String> genre,
			Pageable pageable
	) {
		String cacheEntryKey = cacheKey.asStringKey();
		Cache cache = sheetPostCacheCoordinator.getRequiredCache(SheetPostCacheCoordinator.SHEET_POST_LIST_CACHE);
		return sheetPostCacheCoordinator.getWithSWR(
				cache,
				cacheEntryKey,
				() -> loadSheetPostListPayload(searchSentence, instrument, difficulty, genre, pageable),
				SheetPostCacheCoordinator.LIST_SOFT_TTL_MIN_MS,
				SheetPostCacheCoordinator.LIST_SOFT_TTL_MAX_MS,
				SheetPostCacheCoordinator.LIST_HARD_TTL_MS
		);
	}

	private SheetPostDetailCachePayload getSheetPostDetailWithSWR(Long id) {
		Cache cache = sheetPostCacheCoordinator.getRequiredCache(SheetPostCacheCoordinator.SHEET_POST_DETAIL_CACHE);
		return sheetPostCacheCoordinator.getWithSWR(
				cache,
				id,
				() -> loadSheetPostDetailPayload(id),
				SheetPostCacheCoordinator.DETAIL_SOFT_TTL_MIN_MS,
				SheetPostCacheCoordinator.DETAIL_SOFT_TTL_MAX_MS,
				SheetPostCacheCoordinator.DETAIL_HARD_TTL_MS
		);
	}

	private SheetPostListCachePayload loadSheetPostListPayload(
			String searchSentence,
			List<String> instrument,
			List<String> difficulty,
			List<String> genre,
			Pageable pageable
	) {
		Pair<Page<Long>, String> pairs = elasticSearchInstance.searchSheetPost(
				searchSentence,
				instrument,
				difficulty,
				genre,
				pageable);
		Page<Long> sheetPostIds = pairs.getFirst();
		String rawQuery = pairs.getSecond();
		List<SheetPostListDto> rows = sheetPostIds.getContent().isEmpty()
				? List.of()
				: sheetPostRepository.findByIds(sheetPostIds.getContent(), pageable);
		return new SheetPostListCachePayload(
				copySheetPostListDtos(rows),
				sheetPostIds.getTotalElements(),
				rawQuery
		);
	}

	private SheetPostDetailCachePayload loadSheetPostDetailPayload(Long id) {
		SheetPost sheetPost = sheetPostRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id));
		SheetPostDto baseDto = sheetPost.toDto();
		baseDto.setLikeCount(0);
		baseDto.setViewCount(0);
		baseDto.setLikePost(false);
		return new SheetPostDetailCachePayload(baseDto, sheetPost.getViewCount());
	}

	private List<SheetPostListDto> copySheetPostListDtos(List<SheetPostListDto> source) {
		List<SheetPostListDto> copied = new ArrayList<>(source.size());
		for (SheetPostListDto dto : source) {
			SheetPostListDto copy = new SheetPostListDto(
					dto.getId(),
					dto.getTitle(),
					dto.getArtistName(),
					dto.getArtistProfile(),
					dto.getSheetTitle(),
					dto.getDifficulty(),
					dto.getGenres(),
					dto.getInstrument(),
					dto.getCreatedAt(),
					dto.getPrice());
			copy.updateViewCount(dto.getViewCount());
			copied.add(copy);
		}
		return copied;
	}

	private SheetPostDto copySheetPostDto(SheetPostDto source) {
		ArtistInfo artist = source.getArtist() == null
				? null
				: ArtistInfo.builder()
				.id(source.getArtist().getId())
				.name(source.getArtist().getName())
				.username(source.getArtist().getUsername())
				.email(source.getArtist().getEmail())
				.profileSrc(source.getArtist().getProfileSrc())
				.build();

		SheetInfoDto sheet = source.getSheet() == null
				? null
				: SheetInfoDto.builder()
				.id(source.getSheet().getId())
				.title(source.getSheet().getTitle())
				.content(source.getSheet().getContent())
				.createdAt(source.getSheet().getCreatedAt())
				.artist(source.getSheet().getArtist())
				.genres(source.getSheet().getGenres())
				.pageNum(source.getSheet().getPageNum())
				.difficulty(source.getSheet().getDifficulty())
				.instrument(source.getSheet().getInstrument())
				.isSolo(source.getSheet().isSolo())
				.lyrics(source.getSheet().isLyrics())
				.sheetUrl(source.getSheet().getSheetUrl())
				.originalFileName(source.getSheet().getOriginalFileName())
				.thumbnailUrl(source.getSheet().getThumbnailUrl())
				.build();

		List<com.omegafrog.My.piano.app.web.dto.comment.CommentDto> comments =
				source.getComments() == null ? List.of() : new ArrayList<>(source.getComments());

		SheetPostDto copied = SheetPostDto.builder()
				.id(source.getId())
				.title(source.getTitle())
				.content(source.getContent())
				.discountRate(source.getDiscountRate())
				.artist(artist)
				.sheet(sheet)
				.likePost(source.isLikePost())
				.price(source.getPrice())
				.viewCount(source.getViewCount())
				.likeCount(source.getLikeCount())
				.createdAt(source.getCreatedAt())
				.comments(comments)
				.disabled(source.getDisabled())
				.build();
		copied.setLikePost(source.isLikePost());
		return copied;
	}

}
