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
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SheetPostApplicationService implements CommentHandler {

    private final SheetPostRepository sheetPostRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

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
                        .genre(sheetDto.getGenre())
                        .difficulty(sheetDto.getDifficulty())
                        .lyrics(sheetDto.getLyrics())
                        .pageNum(sheetDto.getPageNum())
                        .isSolo(sheetDto.getIsSolo())
                        .pageNum(sheetDto.getPageNum())
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
        user.addWroteComments(savedComment);


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
