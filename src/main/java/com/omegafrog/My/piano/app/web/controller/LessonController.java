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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public JsonAPISuccessResponse createLesson(@Validated @NotNull @RequestBody LessonRegisterDto lessonRegisterDto) {
        LessonDto lessonDto = lessonService.createLesson(lessonRegisterDto);
        return new ApiSuccessResponse("Create new Lesson success", lessonDto);
    }

    @GetMapping("/lessons")
    public JsonAPISuccessResponse getLessons(@PageableDefault(page=0,size = 10) Pageable pageable) {
        List<LessonDto> allLessons = lessonService.getAllLessons(pageable);
        return new ApiSuccessResponse("Success load all lessons.", allLessons);
    }

    @GetMapping("/lesson/{id}")
    public JsonAPISuccessResponse getLesson(@Valid @NotNull @PathVariable Long id){
        LessonDto lessonById = lessonService.getLessonById(id);
        return new ApiSuccessResponse("Success load lesson" + id + ".", lessonById);
    }

    @PostMapping("/lesson/{id}")
    public JsonAPISuccessResponse updateLesson(
            @Valid @NotNull @RequestBody UpdateLessonDto updateLessonDto,
            @Valid @NotNull @PathVariable Long id) {
        LessonDto updated = lessonService.updateLesson(id, updateLessonDto);
        return new ApiSuccessResponse("Lesson update success", updated);
    }

    @DeleteMapping("/lesson/{id}")
    public JsonAPISuccessResponse deleteLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.deleteLesson(id);
        return new ApiSuccessResponse("Lesson delete success");
    }

    @PutMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse likeLesson(
            @Valid @NotNull @PathVariable Long id){
        lessonService.likeLesson(id);
        return new ApiSuccessResponse("Like lesson success");
    }

    @GetMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse isLikeLesson(
            @Valid @NotNull @PathVariable Long id) {
        boolean isLiked = lessonService.isLikedLesson(id);
        return new ApiSuccessResponse("Check Lesson is liked success.",isLiked);
    }

    @DeleteMapping("/lesson/{id}/like")
    public JsonAPISuccessResponse dislikeLesson(
            @Valid @NotNull @PathVariable Long id){
        lessonService.dislikeLesson(id);
        return new ApiSuccessResponse("Dislike lesson success.");
    }

    @GetMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse isScrappedLesson(
            @Valid @NotNull @PathVariable Long id){
        boolean scrappedLesson = lessonService.isScrappedLesson(id);
        return new ApiSuccessResponse("Check lesson is scrapped success.", scrappedLesson);
    }

    @PutMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse scrapLesson(
            @Valid @NotNull @PathVariable Long id ){
        lessonService.scrapLesson(id);
        return new ApiSuccessResponse("Scrap lesson success.");
    }

    @DeleteMapping("/lesson/{id}/scrap")
    public JsonAPISuccessResponse unScrapLesson(
            @Valid @NotNull @PathVariable Long id){
        lessonService.unScrapLesson(id);
        return new ApiSuccessResponse("Cancel scrap lesson success.");
    }
}
