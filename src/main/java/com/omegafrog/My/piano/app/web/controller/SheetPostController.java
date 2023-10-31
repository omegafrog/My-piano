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
