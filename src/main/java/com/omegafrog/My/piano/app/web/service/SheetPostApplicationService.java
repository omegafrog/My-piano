package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SheetPostApplicationService implements CommentHandler {

    private final SheetPostRepository sheetPostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    private final ElasticSearchInstance elasticSearchInstance;
    private final S3UploadFileExecutor uploadFileExecutor;



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
        try{
            for(MultipartFile file : files){
                List<String> filename = Arrays.stream(file.getOriginalFilename().split("\\.")).toList();
                log.info("filename:{}", filename);
                String contentType = switch (filename.get(1)) {
                    case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
                    case "png" -> MediaType.IMAGE_PNG_VALUE;
                    default -> throw new IllegalArgumentException("Wrong image type.");
                };
                ObjectMetadata metadata = ObjectMetadata.builder().contentType(contentType).build();
                File temp = File.createTempFile("temp", ".data");
                temp.deleteOnExit();
                ReadableByteChannel src = file.getResource().readableChannel();
                FileChannel dest = new FileOutputStream(temp).getChannel();
                dest.transferFrom(src,0, file.getSize());
                uploadFileExecutor.uploadSheet(temp, file.getOriginalFilename(), metadata);
            }
        }catch (IOException e){
            e.printStackTrace();
            throw e;
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
