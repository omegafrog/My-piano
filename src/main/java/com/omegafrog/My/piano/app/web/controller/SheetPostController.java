package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import io.awspring.cloud.s3.S3Resource;
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

    @PostMapping("/file")
    public JsonAPIResponse uploadSheetFile(@RequestParam List<MultipartFile> imgFiles) throws IOException {
        Map<String, S3Resource> s3Resources = sheetPostService.uploadSheetPost(imgFiles);
        Map<String, String> s3Url = new HashMap<>();
        for(String originalFileName : s3Resources.keySet()){
            s3Url.put(originalFileName, s3Resources.get(originalFileName).getURL().toString());
        }
        Map<String, Object> data = ResponseUtil.getStringObjectMap("resources", s3Url);
        return new APISuccessResponse("Upload sheetFile success.", data, objectMapper);
    }

    @GetMapping("/{id}")
    public JsonAPIResponse getSheetPost(@PathVariable Long id)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        SheetPostDto sheetPost = sheetPostService.getSheetPost(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", sheetPost);
        return new APISuccessResponse("Get Sheet post success.", data, objectMapper);
    }

    @GetMapping("")
    public JsonAPIResponse getSheetPosts(Pageable pageable)
            throws AccessDeniedException, PersistenceException, JsonProcessingException {
        List<SheetPostDto> sheetPosts = sheetPostService.getSheetPosts(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPosts", sheetPosts);
        return new APISuccessResponse("Get all sheet post success.", data, objectMapper);
    }

    @PostMapping("write")
    public JsonAPIResponse writeSheetPost(@RequestBody RegisterSheetPostDto dto)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        SheetPostDto sheetPostDto = sheetPostService.writeSheetPost(dto, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", sheetPostDto);
        return new APISuccessResponse("Write sheet post success.", data, objectMapper);
    }

    @PostMapping("{id}")
    public JsonAPIResponse updateSheetPost(@PathVariable Long id, @RequestBody UpdateSheetPostDto dto)
            throws AccessDeniedException, PersistenceException, JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        SheetPostDto update = sheetPostService.update(id, dto, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheetPost", update);
        return new APISuccessResponse("Update sheet post success.", data, objectMapper);
    }

    @DeleteMapping("{id}")
    public JsonAPIResponse deleteSheetPost(@PathVariable Long id)
            throws AccessDeniedException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        sheetPostService.delete(id, loggedInUser);
        return new APISuccessResponse("Delete sheet post success.");
    }
}
