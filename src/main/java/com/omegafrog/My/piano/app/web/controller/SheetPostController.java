package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

@Tag(name = "악보 API 컨트롤러", description = "악보 CRUD를 담당합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sheet-post")
@Slf4j
public class SheetPostController {

    private final SheetPostApplicationService sheetPostService;

    private final MapperUtil mapperUtil;

    @GetMapping("/{id}")
    @Operation(summary = "악보 게시글 조회", description = "유저가 작성한 악보 게시글을 조회한다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public JsonAPIResponse<SheetPostDto> getSheetPost(
            @Valid @NotNull @PathVariable Long id) {
        SheetPostDto data = sheetPostService.getSheetPost(id);
        return new ApiResponse<>("Get Sheet post success.", data);
    }


    // TODO : elasticsearch에서 페이지네이션하고 totalElementCount도 받아와야 함. count가 제대로 집계되지 않음.
    @GetMapping("")
    public JsonAPIResponse<Page<SheetPostListDto>> getSheetPosts(
            @Nullable @RequestParam String searchSentence,
            @Nullable @RequestParam List<String> instrument,
            @Nullable @RequestParam List<String> difficulty,
            @Nullable @RequestParam List<String> genre,
            Pageable pageable) throws IOException {
        Page<SheetPostListDto> data = sheetPostService.getSheetPosts(searchSentence, instrument, difficulty, genre, pageable);
        return new ApiResponse<>("Get sheet posts success.", data);
    }

    @PutMapping("/{id}/like")
    public JsonAPIResponse<Void> likePost(@PathVariable Long id) {
        sheetPostService.likePost(id);
        return new ApiResponse<>("Increase like count success.");
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse isLikePost(@PathVariable Long id) {
        boolean isLiked = sheetPostService.isLikedSheetPost(id);
        return new ApiResponse<>("Check liked sheet post success.", isLiked);
    }

    @DeleteMapping("/{id}/like")
    public JsonAPIResponse<Void> dislikePost(@PathVariable Long id) {
        sheetPostService.dislikeSheetPost(id);
        return new ApiResponse<>("Dislike this sheet post success.");
    }

    @PostMapping()
    public JsonAPIResponse<SheetPostDto> writeSheetPost(
            @RequestPart(name = "sheetFiles") @Valid @NotNull List<MultipartFile> file,
            @RequestPart(name = "sheetInfo") String registerSheetInfo)
            throws IOException {

        RegisterSheetPostDto dto = mapperUtil.parseRegisterSheetPostInfo(registerSheetInfo);
        SheetPostDto data = sheetPostService.writeSheetPost(dto, file);

        return new ApiResponse<>("Write sheet post success.", data);
    }

    @PutMapping("/{id}/scrap")
    public JsonAPIResponse<Void> scrapSheetPost(
            @Valid @NotNull @PathVariable Long id) {
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
            @Valid @NotNull @PathVariable Long id) {
        sheetPostService.unScrapSheetPost(id);
        return new ApiResponse<>("Unscrap sheet post success.");
    }

    @PostMapping("{id}")
    public JsonAPIResponse<SheetPostDto> updateSheetPost(
            @Valid @NotNull @PathVariable Long id,
            @Valid @Nullable @RequestPart MultipartFile file,
            @Valid @Nullable @RequestPart String dto)
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
