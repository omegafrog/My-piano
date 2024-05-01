package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import jakarta.persistence.EntityNotFoundException;
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
    @Autowired
    private CommentRepository commentRepository;

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
        return saved.toDto();
    }

    public List<LessonDto> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable)
                .stream().map(Lesson::toDto).toList();
    }

    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        lesson.increaseViewCount();
        return lesson.toDto();
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
        return updated.toDto();
    }

    public void deleteLesson(Long lessonId, User user) {
        Lesson lesson = getLesson(lessonId);
        if (!lesson.getAuthor().equals(user))
            throw new AccessDeniedException("Cannot update other user's lesson.");
        lessonRepository.deleteById(lessonId);
    }


    public List<CommentDto> addComment(Long lessonId, RegisterCommentDto dto, User loggedInUser) {
        Lesson lesson = getLesson(lessonId);
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));
        lesson.addComment(Comment.builder()
                .content(dto.getContent())
                .author(user)
                .build());
        return lessonRepository.save(lesson).getComments().stream().map(Comment::toDto).toList();
    }

    public void likeComment(Long id, Long commentId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        lesson.getComments().forEach(
                comment -> {
                    if (comment.getId().equals(commentId))
                        comment.increaseLikeCount();
                }
        );
    }


    public void dislikeComment(Long id, Long commentId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        lesson.getComments().forEach(
                comment -> {
                    if (comment.getId().equals(commentId))
                        comment.decreaseLikeCount();
                }
        );
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

    public void likeLesson(Long id, User user) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        lesson.increaseLikedCount();
        User loggedUser = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + user.getId()));
        loggedUser.likeLesson(lesson);
    }
    public void dislikeLesson(Long id, User user){
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + user.getId()));
        if(!loggedUser.isLikedLesson(lesson)) throw new IllegalArgumentException("이 글에 좋아요를 누르지 않았습니다.");
        lesson.decreaseLikedCount();
        loggedUser.dislikeLesson(lesson);
    }

    public boolean isLikedLesson(Long id, User loggedInUser) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        return loggedUser.isLikedLesson(lesson);
    }

    public boolean isScrappedLesson(Long id, User loggedInUser) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        return loggedUser.isScrappedLesson(lesson);
    }

    public void scrapLesson(Long id, User loggedInUser) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        loggedUser.scrapLesson(lesson);
    }

    public void unScrapLesson(Long id, User loggedInUser) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        loggedUser.unScrapLesson(lesson);
    }


    public CommentDto replyComment(Long id,Long commentId, String replyContent, User loggedInUser) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        Comment comment = lesson.getComments().stream().filter(item -> item.getId().equals(commentId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Cannot find comment entity : " + commentId));
        commentRepository.findById(commentId)
                        .orElseThrow(()->new EntityNotFoundException("Cannot find comment entity : " + commentId));
        Comment reply = Comment.builder().content(replyContent)
                .author(loggedInUser)
                .build();
        Comment saved = commentRepository.save(reply);
        comment.addReply(saved);
        return saved.toDto();
    }
}
