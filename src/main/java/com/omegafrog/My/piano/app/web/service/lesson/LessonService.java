package com.omegafrog.My.piano.app.web.service.lesson;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.RegisterLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonService {
    private static final Logger log = LoggerFactory.getLogger(LessonService.class);
    private final SheetPostRepository sheetPostRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final LessonViewCountRepository lessonViewCountRepository;
    private final AuthenticationUtil authenticationUtil;
    @Qualifier("LessonLikeCountRepositoryImpl")
    @Autowired
    private LikeCountRepository lessonLikeCountRepository;

    public LessonDto createLesson(RegisterLessonDto registerLessonDto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();

        SheetPost sheetPost = sheetPostRepository.findById(registerLessonDto.getSheetPostId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + registerLessonDto.getSheetPostId()));

        Lesson lesson = Lesson.builder()
                .sheetPost(sheetPost)
                .lessonProvider(loggedInUser)
                .title(registerLessonDto.getTitle())
                .subTitle(registerLessonDto.getSubTitle())
                .videoInformation(registerLessonDto.getVideoInformation())
                .lessonInformation(registerLessonDto.getLessonInformation())
                .lessonProvider(loggedInUser)
                .price(registerLessonDto.getPrice())
                .build();

        Lesson saved = lessonRepository.save(lesson);
        loggedInUser.getUploadedLessons().add(saved);
        return saved.toDto();
    }

    public List<LessonDto> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable)
                .stream().map(Lesson::toDto).toList();
    }

    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));

        LessonDto dto = lesson.toDto();

        int incrementedViewCount = lessonViewCountRepository.incrementViewCount(lesson);
        LikeCount byId = lessonLikeCountRepository.findById(id);
        dto.setViewCount(incrementedViewCount);
        dto.setLikeCount(byId.getLikeCount());
        return dto;
    }

    public LessonDto updateLesson(Long lessonId, UpdateLessonDto updateLessonDto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + lessonId));
        if (!lesson.getAuthor().equals(loggedInUser))
            throw new AccessDeniedException("Cannot update other user's lesson.");

        SheetPost sheetPost = sheetPostRepository.findBySheetId(updateLessonDto.getSheetId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + updateLessonDto.getSheetId()));
        Lesson updated = lesson.update(updateLessonDto, sheetPost);
        return updated.toDto();
    }

    public void deleteLesson(Long lessonId) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = getLesson(lessonId);
        if (!lesson.getAuthor().equals(loggedInUser))
            throw new AccessDeniedException("Cannot update other user's lesson.");
        lessonRepository.deleteById(lessonId);
    }

    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + lessonId));
    }

    public void likeLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        lesson.increaseLikedCount();

        loggedInUser.likeLesson(lesson);
        lessonLikeCountRepository.incrementLikeCount(lesson);
    }

    public void dislikeLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        if (!loggedInUser.isLikedLesson(lesson)) throw new IllegalArgumentException("이 글에 좋아요를 누르지 않았습니다.");

        lessonLikeCountRepository.decrementLikeCount(lesson);
        loggedInUser.dislikeLesson(lesson);
    }

    public boolean isLikedLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        return loggedInUser.isLikedLesson(lesson);
    }

    public boolean isScrappedLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        return loggedUser.isScrappedLesson(lesson);
    }

    public void scrapLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        loggedUser.scrapLesson(lesson);
    }

    public void unScrapLesson(Long id) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + id));
        User loggedUser = userRepository.findById(loggedInUser.getId()).orElseThrow(() -> new EntityNotFoundException("Cannot find user entity : " + loggedInUser.getId()));
        loggedUser.unScrapLesson(lesson);
    }
}
