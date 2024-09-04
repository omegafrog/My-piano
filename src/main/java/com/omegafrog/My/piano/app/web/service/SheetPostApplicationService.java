package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
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
import com.omegafrog.My.piano.app.web.exception.WrongFileExtensionException;
import io.awspring.cloud.s3.ObjectMetadata;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SheetPostApplicationService {
    private final SheetPostIndexRepository sheetPostIndexRepository;

    private final SheetPostRepository sheetPostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    private final ElasticSearchInstance elasticSearchInstance;
    private final S3UploadFileExecutor uploadFileExecutor;
    private final MapperUtil mapperUtil;
    private final SheetPostViewCountRepository sheetPostViewCountRepository;
    private final AuthenticationUtil authenticationUtil;


    public SheetPostDto getSheetPost(Long id) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id));
        int incremented = sheetPostViewCountRepository.incrementViewCount(sheetPost);
        SheetPostDto dto = sheetPost.toDto();
        dto.setViewCount(incremented);
        return dto;
    }

    public SheetPostDto writeSheetPost(
            RegisterSheetPostDto dto,
            List<MultipartFile> files) throws IOException {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        try {
            MultipartFile file = files.get(0);

            SheetPostTempFile tempFile = getSheetPostTempFile(file);

            uploadFileExecutor.uploadSheet(tempFile.temp(), file.getOriginalFilename(), tempFile.metadata);
            uploadFileExecutor.uploadThumbnail(
                    tempFile.document, tempFile.filename,
                    new ObjectMetadata.Builder().contentType("jpg").build());

            Sheet sheet = dto.getSheet().createEntity(loggedInUser, tempFile.pageNum);

            SheetPost sheetPost = SheetPost.builder()
                    .title(dto.getTitle())
                    .sheet(sheet)
                    .artist(loggedInUser)
                    .price(dto.getPrice())
                    .content(dto.getContent())
                    .build();

            SheetPost saved = sheetPostRepository.save(sheetPost);
            elasticSearchInstance.invertIndexingSheetPost(saved);
            return saved.toDto();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            throw e;
        }
    }

    public SheetPostDto update(Long id, String dto, MultipartFile file) throws IOException {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        UpdateSheetPostDto updateDto = mapperUtil.parseUpdateSheetPostJson(dto);
        log.debug("updateDto : {}", updateDto);
        if (sheetPost.getAuthor().equals(loggedInUser)) {
            SheetPostTempFile tmp = getSheetPostTempFile(file);

            // 기존 파일 삭제 후 다시 업로드
            uploadFileExecutor.removeSheetPost(sheetPost);
            uploadFileExecutor.uploadSheet(tmp.temp(), file.getOriginalFilename(), tmp.metadata());
            uploadFileExecutor.uploadThumbnail(
                    tmp.document(), tmp.filename(),
                    new ObjectMetadata.Builder().contentType("jpg").build());

            updateDto.getSheet().setPageNum(tmp.pageNum());
            return sheetPost.update(updateDto).toDto();
        } else throw new AccessDeniedException("Cannot update other user's sheet post." + id);
    }

    private static SheetPostTempFile getSheetPostTempFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String contentType = filename.split("\\.")[1];
        if (!contentType.equals("pdf"))
            throw new WrongFileExtensionException("Cannot save file extension. " + filename);
        ObjectMetadata metadata = ObjectMetadata.builder().contentType(contentType).build();

        File temp = File.createTempFile("temp", ".data");
        FileOutputStream dest = new FileOutputStream(temp);
        dest.write(file.getBytes());
        dest.close();

        PDDocument document = Loader.loadPDF(temp);
        int pageNum = document.getNumberOfPages();
        temp.deleteOnExit();
        return new SheetPostTempFile(filename, metadata, temp, document, pageNum);
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

    private record SheetPostTempFile(String filename, ObjectMetadata metadata, File temp, PDDocument document,
                                     int pageNum) {
    }

    public void delete(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        uploadFileExecutor.removeSheetPost(sheetPost);
        sheetPostIndexRepository.deleteById(id);
        if (sheetPost.getAuthor().equals(loggedInUser))
            sheetPostRepository.deleteById(sheetPost.getId());
        else throw new AccessDeniedException("Cannot delete other user's sheet post entity : " + id);
    }

    public void likePost(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity:" + id));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity:" + id));
        user.likeSheetPost(sheetPost);
    }

    public boolean isLikedSheetPost(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost targetSheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(("Cannot find SheetPost entity:" + id)));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity:" + id));
        return user.getLikedSheetPosts()
                .stream().anyMatch(likedSheetPost -> likedSheetPost.getSheetPost().equals(targetSheetPost));
    }

    public void dislikeSheetPost(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        boolean removed = user.getLikedSheetPosts().removeIf(item -> item.getSheetPost().getId().equals(id));
        if (!removed) throw new EntityNotFoundException("좋아요를 누르지 않았습니다." + id);
        sheetPost.decreaseLikedCount();
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
        return PageableExecutionUtils.getPage(res, pageable, sheetPostIds::getTotalElements);
    }
}
