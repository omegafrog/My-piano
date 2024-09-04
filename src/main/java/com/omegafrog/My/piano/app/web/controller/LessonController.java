package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.RegisterLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
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


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("")
    public JsonAPIResponse createLesson(@Validated @NotNull @RequestBody RegisterLessonDto registerLessonDto) {
        LessonDto lessonDto = lessonService.createLesson(registerLessonDto);
        return new ApiResponse("Create new Lesson success", lessonDto);
    }

    @GetMapping("")
    public JsonAPIResponse getLessons(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        List<LessonDto> allLessons = lessonService.getAllLessons(pageable);
        return new ApiResponse("Success load all lessons.", allLessons);
    }

    @GetMapping("/{id}")
    public JsonAPIResponse getLesson(@Valid @NotNull @PathVariable Long id) {
        LessonDto lessonById = lessonService.getLessonById(id);
        return new ApiResponse("Success load lesson" + id + ".", lessonById);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updateLesson(
            @Valid @NotNull @RequestBody UpdateLessonDto updateLessonDto,
            @Valid @NotNull @PathVariable Long id) {
        LessonDto updated = lessonService.updateLesson(id, updateLessonDto);
        return new ApiResponse("Lesson update success", updated);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse deleteLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.deleteLesson(id);
        return new ApiResponse("Lesson delete success");
    }

    @PutMapping("/{id}/like")
    public JsonAPIResponse likeLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.likeLesson(id);
        return new ApiResponse("Like lesson success");
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse isLikeLesson(
            @Valid @NotNull @PathVariable Long id) {
        boolean isLiked = lessonService.isLikedLesson(id);
        return new ApiResponse("Check Lesson is liked success.", isLiked);
    }

    @DeleteMapping("/{id}/like")
    public JsonAPIResponse dislikeLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.dislikeLesson(id);
        return new ApiResponse("Dislike lesson success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPIResponse isScrappedLesson(
            @Valid @NotNull @PathVariable Long id) {
        boolean scrappedLesson = lessonService.isScrappedLesson(id);
        return new ApiResponse("Check lesson is scrapped success.", scrappedLesson);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPIResponse scrapLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.scrapLesson(id);
        return new ApiResponse("Scrap lesson success.");
    }

    @DeleteMapping("/{id}/scrap")
    public JsonAPIResponse unScrapLesson(
            @Valid @NotNull @PathVariable Long id) {
        lessonService.unScrapLesson(id);
        return new ApiResponse("Cancel scrap lesson success.");
    }
}
