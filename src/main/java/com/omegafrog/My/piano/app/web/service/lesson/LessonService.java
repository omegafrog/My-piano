package com.omegafrog.My.piano.app.web.service.lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
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

    public LessonDto createLesson(LessonRegisterDto lessonRegisterDto, User artist) {
        SheetPost sheetPost = sheetPostRepository.findById(lessonRegisterDto.getSheetPostId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + lessonRegisterDto.getSheetPostId()));
        User user = userRepository.findById(artist.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + artist.getId()));
        Lesson lesson = Lesson.builder()
                .sheetPost(sheetPost)
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
        Lesson updated = lesson.update(updateLessonDto, sheetPost);
        return updated.toDto();
    }

    public void deleteLesson(Long lessonId, User user) {
        Lesson lesson = getLesson(lessonId);
        if (!lesson.getAuthor().equals(user))
            throw new AccessDeniedException("Cannot update other user's lesson.");
        lessonRepository.deleteById(lessonId);
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

    public void scrapLesson(Long id,  User loggedInUser) {
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
}
