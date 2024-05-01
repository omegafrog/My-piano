package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sheet-post")
@Slf4j
public class SheetPostController {

    private final SheetPostApplicationService sheetPostService;
    @Autowired
    private MapperUtil mapperUtil;

    @GetMapping("/{id}")
    public JsonAPIResponse<SheetPostDto> getSheetPost(@PathVariable Long id)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        SheetPostDto data = sheetPostService.getSheetPost(id);
        return new APISuccessResponse<>("Get Sheet post success.", data);
    }

    @GetMapping("")
    public JsonAPIResponse<Page<SheetPostDto>> getSheetPosts(@Nullable @RequestParam String searchSentence,
                                                             @Nullable @RequestParam List<String> instrument,
                                                             @Nullable @RequestParam List<String> difficulty,
                                                             @Nullable @RequestParam List<String> genre,
                                                             Pageable pageable) throws IOException {
        List<SheetPostDto> sheetPosts = sheetPostService.getSheetPosts(searchSentence, instrument, difficulty, genre, pageable);
        Page<SheetPostDto> data = PageableExecutionUtils.getPage(sheetPosts, pageable, sheetPosts::size);
        return new APISuccessResponse<>("Get sheet posts success.", data);
    }
    @PutMapping("/{id}/like")
    public JsonAPIResponse<Void> likePost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.likePost(id, loggedInUser);
        return new APISuccessResponse<>("Increase like count success.");
    }
    @GetMapping("/{id}/like")
    public JsonAPIResponse<Boolean> isLikePost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean data = sheetPostService.isLikedPost(id, loggedInUser);
        return new APISuccessResponse<>("Check liked sheet post success.", data);
    }
    @DeleteMapping("/{id}/like")
    public JsonAPIResponse<Void> dislikePost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.dislikePost(id, loggedInUser);
        return new APISuccessResponse<>("Dislike this sheet post success.");
    }

    @PostMapping("write")
    public JsonAPIResponse<SheetPostDto> writeSheetPost(@RequestPart(name = "sheetFiles") List<MultipartFile> file,
                                          @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();

        RegisterSheetPostDto dto = mapperUtil.parseRegisterSheetPostInfo(registerSheetInfo, loggedInUser);
        SheetPostDto data = sheetPostService.writeSheetPost(dto, file, loggedInUser);

        return new APISuccessResponse<>("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPIResponse<Void> scrapSheetPost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.scrapSheetPost(id, loggedInUser);
        return new APISuccessResponse<>("Scrap sheet post success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPIResponse<Boolean> isScrappedSheetPost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isScrapped = sheetPostService.isScrappedSheetPost(id, loggedInUser);
        return new APISuccessResponse<>("Check sheet post scrap success.", isScrapped);
    }


    @PutMapping("{id}")
    public JsonAPIResponse<SheetPostDto> updateSheetPost(@PathVariable Long id, @RequestParam String dto, @RequestPart MultipartFile file)
            throws AccessDeniedException, PersistenceException, IOException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        SheetPostDto data = sheetPostService.update(id, dto, file, loggedInUser);
        return new APISuccessResponse<>("Update sheet post success.", data);
    }

    @GetMapping("{id}/sheet")
    public JsonAPIResponse<String> getSheetFileUrl(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        String url = sheetPostService.getSheetUrl(id, loggedInUser);
        return new APISuccessResponse<>("Get sheet url success", url);
    }

    @DeleteMapping("{id}")
    public JsonAPIResponse<Void> deleteSheetPost(@PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.delete(id, loggedInUser);
    return new APISuccessResponse<>("Delete sheet post success.");
    }
}
