package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileAccessController {

    private final FileUploadProcessRepository fileUploadProcessRepository;
    private final SheetPostRepository sheetPostRepository;
    private final AuthenticationUtil authenticationUtil;

    @Value("${local.storage.base-path:./local-storage}")
    private String basePath;

    @GetMapping("/sheets/{fileName}")
    public ResponseEntity<Resource> downloadSheet(@PathVariable String fileName) {
        FileUploadProcess process = findLinkedProcess(fileName);
        if (process.getSheetPostId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file is not linked yet");
        }

        User loggedInUser = authenticationUtil.getLoggedInUser();
        SheetPost sheetPost = sheetPostRepository.findById(process.getSheetPostId())
                .orElseThrow(() -> new IllegalArgumentException("SheetPost not found: " + process.getSheetPostId()));
        if (!loggedInUser.isPurchased(sheetPost)) {
            throw new AccessDeniedException("구매하지 않은 sheet post 는 조회할 수 없습니다.");
        }

        return serveFile(Path.of(basePath, "sheets", fileName), MediaType.APPLICATION_PDF);
    }

    @GetMapping("/thumbnails/{fileName}")
    public ResponseEntity<Resource> downloadThumbnail(@PathVariable String fileName) {
        findLinkedProcess(fileName);
        return serveFile(Path.of(basePath, "thumbnails", fileName), MediaType.IMAGE_JPEG);
    }

    private FileUploadProcess findLinkedProcess(String fileName) {
        FileUploadProcess process = fileUploadProcessRepository.findByUuidFileName(fileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + fileName));

        if (!process.isUploadCompleted() || !process.isLinked()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file is not ready");
        }

        return process;
    }

    private ResponseEntity<Resource> serveFile(Path path, MediaType mediaType) {
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + path.getFileName());
        }

        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }
}
