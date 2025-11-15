package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.fileUpload.FileUploadResponse;
import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "파일 업로드 API 컨트롤러", description = "파일 업로드를 담당합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "PDF 파일 업로드", description = "PDF 파일을 업로드하고 uploadId를 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "파일 업로드 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 형식")
    })
    public JsonAPIResponse<FileUploadResponse> uploadFile(
            @RequestParam("file") @Valid @NotNull MultipartFile file) throws IOException {

        log.info("File upload request received: {}", file.getOriginalFilename());

        FileUploadResponse response = fileUploadService.uploadFile(file);

        if (response.getStatus() == FileUploadStatus.FAILED) {
            return new ApiResponse<>("파일 업로드 시작에 실패했습니다.", response);
        }

        return new ApiResponse<>("파일 업로드가 시작되었습니다.", response);
    }

    @GetMapping("/upload/{uploadId}/status")
    @Operation(summary = "파일 업로드 상태 확인", description = "uploadId로 파일 업로드 상태를 확인합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "업로드 ID를 찾을 수 없음")
    })
    public JsonAPIResponse<FileUploadStatus> getUploadStatus(
            @Valid @NotNull @PathVariable String uploadId) {

        log.info("Upload status request for uploadId: {}", uploadId);

        FileUploadStatus status = fileUploadService.getUploadStatus(uploadId);

        if (status == null) {
            return new ApiResponse<>("업로드 ID를 찾을 수 없습니다.", null);
        }

        return new ApiResponse<>("업로드 상태 조회 성공", status);
    }
}