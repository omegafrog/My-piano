package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.service.lesson.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LessonController {

    private final LessonService lessonService;
    @PostMapping("/lesson")
    public JsonAPISuccessResponse createLesson(@Validated @RequestBody LessonRegisterDto lessonRegisterDto)
            throws JsonProcessingException {
        User user = AuthenticationUtil.getLoggedInUser();
        LessonDto lessonDto = lessonService.createLesson(lessonRegisterDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonDto);
        return new ApiSuccessResponse("Create new Lesson success", data);
    }

    @GetMapping("/lessons")
    public JsonAPISuccessResponse getLessons(Pageable pageable) throws JsonProcessingException {
        List<LessonDto> allLessons = lessonService.getAllLessons(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lessons", allLessons);
        return new ApiSuccessResponse("Success load all lessons.", data);
    }

    @GetMapping("/lesson/{id}")
    public JsonAPISuccessResponse getLesson(@PathVariable Long id) throws JsonProcessingException {
        LessonDto lessonById = lessonService.getLessonById(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", lessonById);
        return new ApiSuccessResponse("Success load lesson" + id + ".", data);
    }

    @PostMapping("/lesson/{id}")
    public JsonAPISuccessResponse updateLesson(
            @Validated @RequestBody UpdateLessonDto updateLessonDto, @PathVariable Long id)
            throws JsonProcessingException {
        User user = AuthenticationUtil.getLoggedInUser();
        LessonDto updated = lessonService.updateLesson(id, updateLessonDto, user);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lesson", updated);
        return new ApiSuccessResponse("Lesson update success", data);
    }

    @DeleteMapping("/lesson/{id}")
    public JsonAPISuccessResponse deleteLesson(@PathVariable Long id) {
        User user = AuthenticationUtil.getLoggedInUser();
        lessonService.deleteLesson(id, user);
        return new ApiSuccessResponse("Lesson delete success");
    }

    @PutMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse likeLesson(@PathVariable Long id){
        User user = AuthenticationUtil.getLoggedInUser();
        lessonService.likeLesson(id, user);
        return new ApiSuccessResponse("Like lesson success");
    }

    @GetMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse isLikeLesson(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLiked = lessonService.isLikedLesson(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isLiked", isLiked);
        return new ApiSuccessResponse("Check Lesson is liked success.", data);
    }

    @DeleteMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse dislikeLesson(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.dislikeLesson(id, loggedInUser);
        return new ApiSuccessResponse("Dislike lesson success.");
    }

    @GetMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse isScrappedLesson(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean scrappedLesson = lessonService.isScrappedLesson(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isScrapped", scrappedLesson);
        return new ApiSuccessResponse("Check lesson is scrapped success.", data);
    }

    @PutMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse scrapLesson(@PathVariable Long id ){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.scrapLesson(id, loggedInUser);
        return new ApiSuccessResponse("Scrap lesson success.");
    }

    @DeleteMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse unScrapLesson(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        lessonService.unScrapLesson(id, loggedInUser);
        return new ApiSuccessResponse("Cancel scrap lesson success.");
    }
}
