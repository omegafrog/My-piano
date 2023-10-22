package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.search.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SheetPostApplicationService implements CommentHandler {

    private final SheetPostRepository sheetPostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private final S3Template s3Template;

    private final ElasticSearchInstance elasticSearchInstance;

    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;


    @Transactional
    public SheetPostDto getSheetPost(Long id) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id));
        sheetPost.increaseViewCount();
        return sheetPost.toDto();
    }


    public List<SheetPostDto> getSheetPosts(Pageable pageable) {
        return sheetPostRepository.findAll(pageable).stream()
                .map(SheetPost::toDto).toList();
    }

    public SheetPostDto writeSheetPost(RegisterSheetPostDto dto, List<MultipartFile> files, User loggedInUser) throws IOException {
        Sheet sheet = dto.getSheetDto().getEntityBuilderWithoutAuthor()
                .user(loggedInUser).build();
        SheetPost sheetPost = SheetPost.builder()
                .title(dto.getTitle())
                .sheet(sheet)
                .artist(loggedInUser)
                .price(dto.getPrice())
                .content(dto.getContent())
                .build();

        SheetPost saved = sheetPostRepository.save(sheetPost);

        Map<String, S3Resource> res = new HashMap<>();
        for (MultipartFile file : files) {
            List<String> filename = Arrays.stream(file.getOriginalFilename().split("\\.")).toList();

            String contentType = "";
            switch (filename.get(1)) {
                case "jpg", "jpeg":
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                    break;
                case "png":
                    contentType = MediaType.IMAGE_PNG_VALUE;
                    break;
                default:
                    throw new IllegalArgumentException("Wrong image type.");
            }
            res.put(file.getOriginalFilename(),
                    s3Template.upload(bucketName, file.getOriginalFilename(), file.getInputStream(), ObjectMetadata.builder()
                            .contentType(contentType).build()));
        }
        elasticSearchInstance.invertIndexingSheetPost(saved);
        return saved.toDto();
    }

    public SheetPostDto update(Long id, UpdateSheetPostDto dto, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        if (sheetPost.getAuthor().equals(loggedInUser)) {
            return sheetPost.update(dto).toDto();
        } else throw new AccessDeniedException("Cannot update other user's sheet post." + id);
    }

    public void delete(Long id, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        if (sheetPost.getAuthor().equals(loggedInUser))
            sheetPostRepository.deleteById(sheetPost.getId());
        else throw new AccessDeniedException("Cannot delete other user's sheet post entity : " + id);
    }

    @Override
    public List<CommentDto> getComments(Long articleId, Pageable pageable) {
        SheetPost sheetPost = sheetPostRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + articleId));
        return sheetPost.getComments(pageable).stream().map(Comment::toDto).toList();
    }

    @Override
    public List<CommentDto> addComment(Long id, RegisterCommentDto dto, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));

        Comment savedComment = commentRepository.save(
                Comment.builder()
                        .content(dto.getContent())
                        .author(user)
                        .build());
        sheetPost.addComment(savedComment);
        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }

    @Override
    public List<CommentDto> deleteComment(Long id, Long commentId, User loggedInUser) {
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_COMMENT));
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet post entity : " + id));
        sheetPost.deleteComment(commentId, user);
        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }

    @Override
    public void likeComment(Long id, Long commentId) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        sheetPost.increaseCommentLikeCount(commentId);
    }

    @Override
    public void dislikeComment(Long id, Long commentId) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        sheetPost.decreaseCommentLikeCount(commentId);

    }

    public void likePost(Long id, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity:" + id));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity:" + id));
        user.addLikedSheetPost(sheetPost);
    }

    public boolean isLikedPost(Long id, User loggedInUser) {
        SheetPost targetSheetPost = sheetPostRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(("Cannot find SheetPost entity:" + id)));
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity:" + id));
        return user.getLikedSheetPosts().stream().anyMatch(sheetPost -> sheetPost.equals(targetSheetPost));
    }

    public void scrapSheetPost(Long id, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity:"+ id));
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        user.addScrappedSheetPost(sheetPost);
    }
    public boolean isScrappedSheetPost(Long id, User loggedInUser){
        SheetPost targetSheetPost = sheetPostRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        User user = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        return user.getScrappedSheets().stream().anyMatch(item -> item.equals(targetSheetPost));
    }
}
