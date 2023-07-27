package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.web.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.service.LessonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/lesson")
    public JsonAPIResponse createLesson(@Validated @RequestBody LessonRegisterDto lessonRegisterDto)
            throws JsonProcessingException, EntityNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("authentication is null");
            return new APIInternalServerResponse("authentication is null");
        }
        User user = ((SecurityUser) authentication.getPrincipal()).getUser();
        LessonDto lessonDto = lessonService.createLesson(lessonRegisterDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonDto);
        return new APISuccessResponse("Create new Lesson success", data,objectMapper );
    }

    //TODO : 로그인 상관없이 접근 가능
    @GetMapping("/lesson")
    public JsonAPIResponse getLessons(Pageable pageable) throws JsonProcessingException {
        List<LessonDto> allLessons = lessonService.getAllLessons(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lessons", allLessons);
        return new APISuccessResponse("Success load all lessons.", data, objectMapper);
    }


    //TODO : 로그인 상관없이 접근 가능
    @GetMapping("/lesson/{id}")
    public JsonAPIResponse getLesson(@PathVariable Long id) throws JsonProcessingException {
        LessonDto lessonById = lessonService.getLessonById(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonById);
        return new APISuccessResponse("Success load lesson" + id + ".", data, objectMapper);
    }

    @PostMapping("/lesson/{id}")
    public JsonAPIResponse updateLesson(
            @Validated @RequestBody UpdateLessonDto updateLessonDto,
            @PathVariable Long id)
            throws JsonProcessingException, EntityNotFoundException, AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = getLoggedInUser(authentication);
        LessonDto updated = lessonService.updateLesson(id, updateLessonDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", updated);
        return new APISuccessResponse("Lesson update success", data, objectMapper);
    }

    @DeleteMapping("/lesson/{id}")
    public JsonAPIResponse deleteLesson(
            @PathVariable Long id)
            throws EntityNotFoundException, AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = getLoggedInUser(authentication);
        lessonService.deleteLesson(id, user);
        return new APISuccessResponse("Lesson delete success");
    }
    private static User getLoggedInUser(Authentication authentication) throws org.springframework.security.access.AccessDeniedException {
        if (authentication == null) {
            throw new org.springframework.security.access.AccessDeniedException("authentication is null");
        }
        return ((SecurityUser)authentication.getPrincipal()).getUser();
    }

}
