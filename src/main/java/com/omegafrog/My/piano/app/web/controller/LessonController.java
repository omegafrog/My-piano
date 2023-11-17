package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            throws JsonProcessingException {
        User user = AuthenticationUtil.getLoggedInUser();
        LessonDto lessonDto = lessonService.createLesson(lessonRegisterDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonDto);
        return new APISuccessResponse("Create new Lesson success", data);
    }

    @GetMapping("/lessons")
    public JsonAPIResponse getLessons(Pageable pageable) throws JsonProcessingException {
        List<LessonDto> allLessons = lessonService.getAllLessons(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lessons", allLessons);
        return new APISuccessResponse("Success load all lessons.", data);
    }

    @GetMapping("/lesson/{id}")
    public JsonAPIResponse getLesson(@PathVariable Long id) throws JsonProcessingException {
        LessonDto lessonById = lessonService.getLessonById(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonById);
        return new APISuccessResponse("Success load lesson" + id + ".", data);
    }

    @PostMapping("/lesson/{id}")
    public JsonAPIResponse updateLesson(
            @Validated @RequestBody UpdateLessonDto updateLessonDto, @PathVariable Long id)
            throws JsonProcessingException {
        User user = AuthenticationUtil.getLoggedInUser();
        LessonDto updated = lessonService.updateLesson(id, updateLessonDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", updated);
        return new APISuccessResponse("Lesson update success", data);
    }

    @DeleteMapping("/lesson/{id}")
    public JsonAPIResponse deleteLesson(@PathVariable Long id) {
        User user = AuthenticationUtil.getLoggedInUser();
        lessonService.deleteLesson(id, user);
        return new APISuccessResponse("Lesson delete success");
    }

    @PutMapping("/lesson/{id}/like")
    public JsonAPIResponse likeLesson(@PathVariable Long id){
        User user = AuthenticationUtil.getLoggedInUser();
        lessonService.likeLesson(id, user);
        return new APISuccessResponse("Like lesson success");
    }

    @GetMapping("/lesson/{id}/like")
    public JsonAPIResponse isLikeLesson(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLiked = lessonService.isLikedLesson(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isLiked", isLiked);
        return new APISuccessResponse("Check Lesson is liked success.", data);
    }

    @DeleteMapping("/lesson/{id}/like")
    public JsonAPIResponse dislikeLesson(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.dislikeLesson(id, loggedInUser);
        return new APISuccessResponse("Dislike lesson success.");
    }

    @GetMapping("/lesson/{id}/scrap")
    public JsonAPIResponse isScrappedLesson(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean scrappedLesson = lessonService.isScrappedLesson(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isScrapped", scrappedLesson);
        return new APISuccessResponse("Check lesson is scrapped success.", data);
    }

    @PutMapping("/lesson/{id}/scrap")
    public JsonAPIResponse scrapLesson(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.scrapLesson(id, loggedInUser);
        return new APISuccessResponse("Scrap lesson success.");
    }

    @DeleteMapping("/lesson/{id}/scrap")
    public JsonAPIResponse unScrapLesson(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.unScrapLesson(id, loggedInUser);
        return new APISuccessResponse("Cancel scrap lesson success.");
    }
}
