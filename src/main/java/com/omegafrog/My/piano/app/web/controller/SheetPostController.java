package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.RegisterSheetDto;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sheet")
@Slf4j
public class SheetPostController {

    private final SheetPostApplicationService sheetPostService;
    @Autowired
    private ObjectMapper objectMapper;



    @GetMapping("/{id}")
    public JsonAPIResponse getSheetPost(@PathVariable Long id)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        SheetPostDto sheetPost = sheetPostService.getSheetPost(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", sheetPost);
        return new APISuccessResponse("Get Sheet post success.", data);
    }


    @GetMapping("")
    public JsonAPIResponse getSheetPosts(Pageable pageable)
            throws AccessDeniedException, PersistenceException, JsonProcessingException {
        List<SheetPostDto> sheetPosts = sheetPostService.getSheetPosts(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPosts", sheetPosts);
        return new APISuccessResponse("Get all sheet post success.", data);
    }
    @PutMapping("/{id}/like")
    public JsonAPIResponse likePost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.likePost(id, loggedInUser);
        return new APISuccessResponse("Increase like count success.");
    }
    @GetMapping("/{id}/like")
    public JsonAPIResponse isLikePost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLikedPost = sheetPostService.isLikedPost(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isLikedPost", isLikedPost);
        return new APISuccessResponse("Check liked sheet post success.", data);
    }

    @PostMapping("write")
    public JsonAPIResponse writeSheetPost(@RequestPart(name = "sheetFiles") List<MultipartFile> file,
                                          @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        JsonNode node = objectMapper.readTree(registerSheetInfo);
        RegisterSheetPostDto dto = RegisterSheetPostDto.builder()
                .title(node.get("title").asText())
                .content(node.get("content").asText())
                .price(node.get("price").asInt())
                .discountRate(node.get("discountRate").asDouble())
                .artistId(loggedInUser.getId())
                .sheetDto(objectMapper.convertValue(node.get("sheetDto"), RegisterSheetDto.class))
                .build();
        SheetPostDto sheetPostDto = sheetPostService.writeSheetPost(dto, file, loggedInUser);

        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", sheetPostDto);
        return new APISuccessResponse("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPIResponse scrapSheetPost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.scrapSheetPost(id, loggedInUser);
        return new APISuccessResponse("Scrap sheet post success.");
    }

    @DeleteMapping("/{id}/scrap")
    public JsonAPIResponse unScrapSheetPost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.unScrapSheetPost(id, loggedInUser);
        return new APISuccessResponse("Unscrap sheet post success.");
    }

    @GetMapping("/{id}/scrap")
    public JsonAPIResponse isScrappedSheetPost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isScrapped = sheetPostService.isScrappedSheetPost(id, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("isScrapped", isScrapped);
        return new APISuccessResponse("Check sheet post scrap success.", data);
    }


    @PostMapping("{id}")
    public JsonAPIResponse updateSheetPost(@PathVariable Long id, @RequestBody UpdateSheetPostDto dto)
            throws AccessDeniedException, PersistenceException, JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        SheetPostDto update = sheetPostService.update(id, dto, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", update);
        return new APISuccessResponse("Update sheet post success.", data);
    }

    @DeleteMapping("{id}")
    public JsonAPIResponse deleteSheetPost(@PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.delete(id, loggedInUser);
        return new APISuccessResponse("Delete sheet post success.");
    }
}
