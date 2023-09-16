package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
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

    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    public Map<String, S3Resource> uploadSheetPost(List<MultipartFile> multipartFile) throws IOException {
        Map<String, S3Resource> res = new HashMap<>();
        for (MultipartFile file : multipartFile) {
            List<String> filename = Arrays.stream(file.getOriginalFilename().split("\\.")).toList();
            String key = "sheet-" + filename.get(0) + UUID.randomUUID() + "." + filename.get(1);

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
                    s3Template.upload(bucketName, key, file.getInputStream(), ObjectMetadata.builder()
                    .contentType(contentType).build()));
        }
        return res;
    }

    public SheetPostDto getSheetPost(Long id) {
        return sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id)).toDto();
    }


    public List<SheetPostDto> getSheetPosts(Pageable pageable) {
        return sheetPostRepository.findAll(pageable).stream()
                .map(SheetPost::toDto).toList();
    }

    public SheetPostDto writeSheetPost(RegisterSheetPostDto dto, User loggedInUser) {
        SheetDto sheetDto = dto.getSheetDto();
        SheetPost sheetPost = SheetPost.builder()
                .title(dto.getTitle())
                .sheet(Sheet.builder()
                        .title(sheetDto.getTitle())
                        .genres(dto.getSheetDto().getGenres())
                        .difficulty(sheetDto.getDifficulty())
                        .lyrics(sheetDto.getLyrics())
                        .pageNum(sheetDto.getPageNum())
                        .isSolo(sheetDto.getIsSolo())
                        .instrument(sheetDto.getInstrument())
                        .user(loggedInUser)
                        .filePath(sheetDto.getFilePath())
                        .difficulty(sheetDto.getDifficulty())
                        .build())
                .artist(loggedInUser)
                .price(dto.getPrice())
                .content(dto.getContent())
                .build();
        return sheetPostRepository.save(sheetPost).toDto();
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
}
