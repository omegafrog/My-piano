package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public JsonAPISuccessResponse<SheetPostDto> getSheetPost(
            @Valid @NotNull @PathVariable Long id) {
        SheetPostDto data = sheetPostService.getSheetPost(id);
        return new ApiSuccessResponse<>("Get Sheet post success.", data);
    }

    @GetMapping("")
    public JsonAPISuccessResponse<Page<SheetPostDto>> getSheetPosts(@Nullable @RequestParam String searchSentence,
                                                                    @Nullable @RequestParam List<String> instrument,
                                                                    @Nullable @RequestParam List<String> difficulty,
                                                                    @Nullable @RequestParam List<String> genre,
                                                                    Pageable pageable) throws IOException {
        Page<SheetPostDto> data= sheetPostService.getSheetPosts(searchSentence, instrument, difficulty, genre, pageable);
        return new ApiSuccessResponse<>("Get sheet posts success.", data);
    }
    @PutMapping("/{id}/like")
    public JsonAPISuccessResponse<Void> likePost(@PathVariable Long id){
        sheetPostService.likePost(id);
        return new ApiSuccessResponse<>("Increase like count success.");
    }
    @GetMapping("/{id}/like")
    public JsonAPISuccessResponse<Map<String, Object>> isLikePost(@PathVariable Long id){
        boolean isLiked= sheetPostService.isLikedSheetPost(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isLiked", isLiked);
        return new ApiSuccessResponse<>("Check liked sheet post success.", data);
    }
    @DeleteMapping("/{id}/like")
    public JsonAPISuccessResponse<Void> dislikePost(@PathVariable Long id){
        sheetPostService.dislikeSheetPost(id);
        return new ApiSuccessResponse<>("Dislike this sheet post success.");
    }

    @PostMapping()
    public JsonAPISuccessResponse<SheetPostDto> writeSheetPost(
            @RequestPart(name = "sheetFiles") @Valid @NotNull List<MultipartFile> file,
            @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException{

        RegisterSheetPostDto dto = mapperUtil.parseRegisterSheetPostInfo(registerSheetInfo);
        SheetPostDto data = sheetPostService.writeSheetPost(dto, file);

        return new ApiSuccessResponse<>("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPISuccessResponse<Void> scrapSheetPost(
            @Valid @NotNull @PathVariable Long id){
        sheetPostService.scrapSheetPost(id);
        return new ApiSuccessResponse<>("Scrap sheet post success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPISuccessResponse isScrappedSheetPost(
            @Valid @NotNull @PathVariable Long id) {
        boolean isScrapped = sheetPostService.isScrappedSheetPost(id);
        return new ApiSuccessResponse<>("Check sheet post scrap success.", isScrapped);
    }

    @DeleteMapping("/{id}/scrap")
    public JsonAPISuccessResponse unScrapSheetPost(
            @Valid @NotNull @PathVariable Long id){
        sheetPostService.unScrapSheetPost(id);
        return new ApiSuccessResponse<>("Unscrap sheet post success.");
    }
    @PutMapping("{id}")
    public JsonAPISuccessResponse<SheetPostDto> updateSheetPost(
            @Valid @NotNull @PathVariable Long id,
            @Valid @Nullable @RequestPart MultipartFile file,
            @Valid @Nullable @RequestParam String dto)
            throws IOException {
        SheetPostDto data = sheetPostService.update(id, dto, file);
        return new ApiSuccessResponse<>("Update sheet post success.", data);
    }

    @GetMapping("{id}/sheet")
    public JsonAPISuccessResponse<String> getSheetFileUrl(
            @Valid @NotNull @PathVariable Long id) {
        String url = sheetPostService.getSheetUrl(id);
        return new ApiSuccessResponse<>("Get sheet url success", url);
    }

    @DeleteMapping("{id}")
    public JsonAPISuccessResponse<Void> deleteSheetPost(
            @Valid @NotNull @PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        sheetPostService.delete(id);
    return new ApiSuccessResponse<>("Delete sheet post success.");
    }
}
