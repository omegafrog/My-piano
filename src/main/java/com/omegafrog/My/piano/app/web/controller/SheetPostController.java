package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sheet-post")
@Slf4j
public class SheetPostController {

    private final SheetPostApplicationService sheetPostService;

    private final MapperUtil mapperUtil;

    @GetMapping("/{id}")
    public JsonAPIResponse<SheetPostDto> getSheetPost(
            @Valid @NotNull @PathVariable Long id) {
        SheetPostDto data = sheetPostService.getSheetPost(id);
        return new ApiResponse<>("Get Sheet post success.", data);
    }

    @GetMapping("")
    public JsonAPIResponse<Page<SheetPostListDto>> getSheetPosts(
            @Nullable @RequestParam String searchSentence,
            @Nullable @RequestParam List<String> instrument,
            @Nullable @RequestParam List<String> difficulty,
            @Nullable @RequestParam List<String> genre,
            Pageable pageable) throws IOException {
        Page<SheetPostListDto> data= sheetPostService.getSheetPosts(searchSentence, instrument, difficulty, genre, pageable);
        return new ApiResponse<>("Get sheet posts success.", data);
    }
    @PutMapping("/{id}/like")
    public JsonAPIResponse<Void> likePost(@PathVariable Long id){
        sheetPostService.likePost(id);
        return new ApiResponse<>("Increase like count success.");
    }
    @GetMapping("/{id}/like")
    public JsonAPIResponse isLikePost(@PathVariable Long id){
        boolean isLiked= sheetPostService.isLikedSheetPost(id);
        return new ApiResponse<>("Check liked sheet post success.", isLiked);
    }
    @DeleteMapping("/{id}/like")
    public JsonAPIResponse<Void> dislikePost(@PathVariable Long id){
        sheetPostService.dislikeSheetPost(id);
        return new ApiResponse<>("Dislike this sheet post success.");
    }

    @PostMapping()
    public JsonAPIResponse<SheetPostDto> writeSheetPost(
            @RequestPart(name = "sheetFiles") @Valid @NotNull List<MultipartFile> file,
            @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException{

        RegisterSheetPostDto dto = mapperUtil.parseRegisterSheetPostInfo(registerSheetInfo);
        SheetPostDto data = sheetPostService.writeSheetPost(dto, file);

        return new ApiResponse<>("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPIResponse<Void> scrapSheetPost(
            @Valid @NotNull @PathVariable Long id){
        sheetPostService.scrapSheetPost(id);
        return new ApiResponse<>("Scrap sheet post success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPIResponse isScrappedSheetPost(
            @Valid @NotNull @PathVariable Long id) {
        boolean isScrapped = sheetPostService.isScrappedSheetPost(id);
        return new ApiResponse<>("Check sheet post scrap success.", isScrapped);
    }

    @DeleteMapping("/{id}/scrap")
    public JsonAPIResponse unScrapSheetPost(
            @Valid @NotNull @PathVariable Long id){
        sheetPostService.unScrapSheetPost(id);
        return new ApiResponse<>("Unscrap sheet post success.");
    }
    @PutMapping("{id}")
    public JsonAPIResponse<SheetPostDto> updateSheetPost(
            @Valid @NotNull @PathVariable Long id,
            @Valid @Nullable @RequestPart MultipartFile file,
            @Valid @Nullable @RequestParam String dto)
            throws IOException {
        SheetPostDto data = sheetPostService.update(id, dto, file);
        return new ApiResponse<>("Update sheet post success.", data);
    }

    @GetMapping("{id}/sheet")
    public JsonAPIResponse<String> getSheetFileUrl(
            @Valid @NotNull @PathVariable Long id) {
        String url = sheetPostService.getSheetUrl(id);
        return new ApiResponse<>("Get sheet url success", url);
    }

    @DeleteMapping("{id}")
    public JsonAPIResponse<Void> deleteSheetPost(
            @Valid @NotNull @PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        sheetPostService.delete(id);
    return new ApiResponse<>("Delete sheet post success.");
    }
}
