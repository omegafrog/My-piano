package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
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
public class SheetPostApplicationService {

    private final SheetPostRepository sheetPostRepository;

    public SheetPostDto getSheetPost(Long id) {
        return sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find SheetPost entity : " + id)).toDto();
    }


    public List<SheetPostDto> getSheetPosts(Pageable pageable) {
        return sheetPostRepository.findAll(pageable).stream()
                .map(SheetPost::toDto).toList();
    }

    public SheetPostDto writeSheetPost(RegisterSheetPostDto dto, User loggedInUser)
            throws PersistenceException {
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

    public SheetPostDto update(Long id, UpdateSheetPostDto dto, User loggedInUser)
            throws EntityNotFoundException, AccessDeniedException {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        if (sheetPost.getAuthor().equals(loggedInUser)) {
            return sheetPost.update(dto).toDto();
        } else throw new AccessDeniedException("Cannot update other user's sheet post." + id);
    }

    public void delete(Long id, User loggedInUser)
            throws EntityNotFoundException, AccessDeniedException {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        if (sheetPost.getAuthor().equals(loggedInUser))
            sheetPostRepository.deleteById(sheetPost.getId());
        else throw new AccessDeniedException("Cannot delete other user's sheet post entity : " + id);
    }

    public List<CommentDto> getComments(Long id)
            throws EntityNotFoundException {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }

    public List<CommentDto> writeComment(Long id, CommentDto dto, User loggedInUser)
            throws PersistenceException {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        sheetPost.addComment(Comment.builder()
                .content(dto.getContent())
                .author(loggedInUser)
                .build());
        return sheetPostRepository.save(sheetPost).toDto().getComments();
    }

    public void deleteComment(Long id, Long commentId, User loggedInUser)
    throws PersistenceException, AccessDeniedException{
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet post entity : " + id));
        sheetPost.deleteComment(commentId, loggedInUser);
    }

    public List<CommentDto> likeComment(Long id, Long commentId) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        sheetPost.getComments().forEach(
                comment -> {
                    if(comment.getId().equals(commentId))
                        comment.increaseLikeCount();
                }
        );

        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }

    public List<CommentDto> dislikeComment(Long id, Long commentId) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + id));
        sheetPost.getComments().forEach(
                comment -> {
                    if(comment.getId().equals(commentId))
                        comment.decreaseLikeCount();
                }
        );
        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }
}
