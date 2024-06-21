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
    @Autowired
    private MapperUtil mapperUtil;

    @GetMapping("/{id}")
    public JsonAPISuccessResponse<SheetPostDto> getSheetPost(@PathVariable Long id)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
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
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.likePost(id, loggedInUser);
        return new ApiSuccessResponse<>("Increase like count success.");
    }
    @GetMapping("/{id}/like")
    public JsonAPISuccessResponse<Map<String, Object>> isLikePost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLiked= sheetPostService.isLikedSheetPost(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isLiked", isLiked);
        return new ApiSuccessResponse<>("Check liked sheet post success.", data);
    }
    @DeleteMapping("/{id}/like")
    public JsonAPISuccessResponse<Void> dislikePost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.dislikeSheetPost(id, loggedInUser);
        return new ApiSuccessResponse<>("Dislike this sheet post success.");
    }

    @PostMapping()
    public JsonAPISuccessResponse<SheetPostDto> writeSheetPost(@RequestPart(name = "sheetFiles") @Valid @NotNull List<MultipartFile> file,
                                                               @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();

        RegisterSheetPostDto dto = mapperUtil.parseRegisterSheetPostInfo(registerSheetInfo, loggedInUser);
        SheetPostDto data = sheetPostService.writeSheetPost(dto, file, loggedInUser);

        return new ApiSuccessResponse<>("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPISuccessResponse<Void> scrapSheetPost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.scrapSheetPost(id, loggedInUser);
        return new ApiSuccessResponse<>("Scrap sheet post success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPISuccessResponse<Map<String, Object>> isScrappedSheetPost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isScrapped = sheetPostService.isScrappedSheetPost(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isScrapped", isScrapped);
        return new ApiSuccessResponse<>("Check sheet post scrap success.", data);
    }

    @DeleteMapping("/{id}/scrap")
    public JsonAPISuccessResponse<Map<String, Object>> unScrapSheetPost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.unScrapSheetPost(id, loggedInUser);
        return new ApiSuccessResponse<>("Unscrap sheet post success.");
    }
    @PutMapping("{id}")
    public JsonAPISuccessResponse<SheetPostDto> updateSheetPost(@PathVariable Long id, @RequestParam String dto, @RequestPart MultipartFile file)
            throws AccessDeniedException, PersistenceException, IOException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        SheetPostDto data = sheetPostService.update(id, dto, file, loggedInUser);
        return new ApiSuccessResponse<>("Update sheet post success.", data);
    }

    @GetMapping("{id}/sheet")
    public JsonAPISuccessResponse<String> getSheetFileUrl(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        String url = sheetPostService.getSheetUrl(id, loggedInUser);
        return new ApiSuccessResponse<>("Get sheet url success", url);
    }

    @DeleteMapping("{id}")
    public JsonAPISuccessResponse<Void> deleteSheetPost(@PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.delete(id, loggedInUser);
    return new ApiSuccessResponse<>("Delete sheet post success.");
    }
}
