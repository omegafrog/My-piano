package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LessonService {

    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private UserRepository userRepository;

    public LessonDto createLesson(LessonRegisterDto lessonRegisterDto, User artist) {
        SheetPost sheetPost = sheetPostRepository.findBySheetId(lessonRegisterDto.getSheetId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + lessonRegisterDto.getSheetId()));
        User user = userRepository.findById(artist.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + artist.getId()));
        Sheet sheet = sheetPost.getSheet();
        Lesson lesson = Lesson.builder()
                .sheet(sheet)
                .lessonProvider(artist)
                .title(lessonRegisterDto.getTitle())
                .subTitle(lessonRegisterDto.getSubTitle())
                .videoInformation(lessonRegisterDto.getVideoInformation())
                .lessonInformation(lessonRegisterDto.getLessonInformation())
                .lessonProvider(artist)
                .price(lessonRegisterDto.getPrice())
                .build();
        Lesson saved = lessonRepository.save(lesson);
        user.getUploadedLessons().add(saved);
        userRepository.save(user);
        return saved.toDto();
    }

    public List<LessonDto> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable)
                .stream().map(Lesson::toDto).toList();
    }

    public LessonDto getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id))
                .toDto();
    }

    public LessonDto updateLesson(Long lessonId, UpdateLessonDto updateLessonDto, User user) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + lessonId));
        if (!lesson.getAuthor().equals(user)) {
            throw new AccessDeniedException("Cannot update other user's lesson.");
        }

        SheetPost sheetPost = sheetPostRepository.findBySheetId(updateLessonDto.getSheetId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + updateLessonDto.getSheetId()));
        Sheet sheet = sheetPost.getSheet();
        Lesson updated = lesson.update(updateLessonDto, sheet);
        return lessonRepository.save(updated).toDto();
    }

    public void deleteLesson(Long lessonId, User user) {
        Lesson lesson = getLesson(lessonId);
        if (!lesson.getAuthor().equals(user))
            throw new AccessDeniedException("Cannot update other user's lesson.");
        lessonRepository.deleteById(lessonId);
    }

    public List<CommentDto> addComment(Long lessonId, CommentDto dto, User loggedInUser) {
        Lesson lesson = getLesson(lessonId);
        lesson.addComment(Comment.builder()
                .content(dto.getContent())
                .author(loggedInUser)
                .build());
        return lessonRepository.save(lesson).getComments().stream().map(Comment::toDto).toList();
    }

    public List<CommentDto> deleteComment(Long lessonId, Long commentId, User loggedInUser) {
        Lesson lesson = getLesson(lessonId);

        boolean isCommentRemoved = lesson.getComments().removeIf(
                comment -> {
                    if (isCommentIdEquals(commentId, comment)) {
                        if (isCommentAuthorEquals(loggedInUser, comment))
                            return true;
                        else throw new AccessDeniedException("Cannot delete other user's comment : " + commentId);
                    }
                    return false;
                }
        );
        if (isCommentRemoved) {
            Lesson saved = lessonRepository.save(lesson);
            return saved.getComments().stream().map(Comment::toDto).toList();
        } else throw new EntityNotFoundException("Cannot find Comment entity : " + commentId);
    }

    public List<CommentDto> getAllComments(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Lesson entity : " + id));
        return lesson.getComments().stream().map(Comment::toDto).toList();
    }

    private static boolean isCommentAuthorEquals(User loggedInUser, Comment comment) {
        return comment.getAuthor().equals(loggedInUser);
    }

    private static boolean isCommentIdEquals(Long commentId, Comment comment) {
        return comment.getId().equals(commentId);
    }


    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + lessonId));
    }

    public List<CommentDto> likeComment(Long id, Long commentId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        lesson.getComments().forEach(
                comment -> {
                    if (comment.getId().equals(commentId))
                        comment.increaseLikeCount();
                }
        );
        Lesson saved = lessonRepository.save(lesson);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }

    public List<CommentDto> dislikeComment(Long id, Long commentId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        lesson.getComments().forEach(
                comment -> {
                    if (comment.getId().equals(commentId))
                        comment.decreaseLikeCount();
                }
        );
        Lesson saved = lessonRepository.save(lesson);
        return saved.getComments().stream().map(Comment::toDto).toList();
    }
}
