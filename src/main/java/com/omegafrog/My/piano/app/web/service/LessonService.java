package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@Transactional
public class LessonService {

    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private LessonRepository lessonRepository;

    public LessonDto createLesson(LessonRegisterDto lessonRegisterDto, User artist) throws EntityNotFoundException{
        SheetPost sheetPost = sheetPostRepository.findBySheetId(lessonRegisterDto.getSheetId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheetPost entity : " + lessonRegisterDto.getSheetId()));
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
        return lessonRepository.save(lesson).toDto();
    }

    public List<LessonDto> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable)
                .stream().map(Lesson::toDto).toList();
    }

    public LessonDto getLessonById(Long id)throws EntityNotFoundException{
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id))
                .toDto();
    }

    public LessonDto updateLesson(Long lessonId, UpdateLessonDto updateLessonDto, User user)
            throws AccessDeniedException, EntityNotFoundException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + lessonId));
        if(!lesson.getAuthor().equals(user)){
            throw new AccessDeniedException("Cannot update other user's lesson.");
        }

        SheetPost sheetPost = sheetPostRepository.findBySheetId(updateLessonDto.getSheetId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + updateLessonDto.getSheetId()));
        Sheet sheet = sheetPost.getSheet();
        Lesson updated = lesson.update(updateLessonDto, sheet);
        return lessonRepository.save(updated).toDto();
    }
    public void deleteLesson(Long lessonId, User user) throws AccessDeniedException, EntityNotFoundException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson Entity : " + lessonId));
        if(!lesson.getAuthor().equals(user)){
            throw new AccessDeniedException("Cannot update other user's lesson.");
        }
        lessonRepository.deleteById(lessonId);
    }
}
