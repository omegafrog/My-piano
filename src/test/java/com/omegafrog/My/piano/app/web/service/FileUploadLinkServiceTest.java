package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadLinkStatus;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadLinkServiceTest {

    @Test
    void processLinkJobMarksLinkedWhenUploadResultAndSheetPostIdPresent() {
        InMemoryRepo repo = new InMemoryRepo();
        SheetPostRepository sheetPostRepository = Mockito.mock(SheetPostRepository.class);
        FileUploadRedisReadModelWriter readModelWriter = Mockito.mock(FileUploadRedisReadModelWriter.class);

        Long sheetPostId = 10L;
        Sheet sheet = Mockito.mock(Sheet.class);
        SheetPost sheetPost = Mockito.mock(SheetPost.class);
        Mockito.when(sheetPost.getSheet()).thenReturn(sheet);
        Mockito.when(sheetPostRepository.findById(sheetPostId)).thenReturn(Optional.of(sheetPost));

        FileUploadLinkService service = new FileUploadLinkService(repo, sheetPostRepository, readModelWriter);

        FileUploadProcess job = FileUploadProcess.builder()
                .uploadId("u1")
                .originalFileName("score.pdf")
                .uuidFileName("uuid.pdf")
                .stagedFilePath("/tmp/stage.pdf")
                .status(FileUploadProcessStatus.COMPLETED)
                .maxAttempts(3)
                .nextAttemptAt(LocalDateTime.now())
                .build();
        job.updateUploadResult("sheetUrl", "thumbUrls", 1);
        job.assignSheetPostId(sheetPostId);
        job.markLinkPending(LocalDateTime.now().minusSeconds(1));

        job = repo.save(job);

        service.processLinkJob(job.getId(), 1);

        FileUploadProcess saved = repo.findById(job.getId()).orElseThrow();
        assertThat(saved.getLinkStatus()).isEqualTo(FileUploadLinkStatus.LINKED);
    }

    private static class InMemoryRepo implements FileUploadProcessRepository {
        private long seq = 0;
        private final Map<Long, FileUploadProcess> store = new HashMap<>();

        @Override
        public FileUploadProcess save(FileUploadProcess job) {
            if (job.getId() == null) {
                ReflectionTestUtils.setField(job, "id", ++seq);
                ReflectionTestUtils.setField(job, "createdAt", LocalDateTime.now());
            }
            ReflectionTestUtils.setField(job, "updatedAt", LocalDateTime.now());
            store.put(job.getId(), job);
            return job;
        }

        @Override
        public Optional<FileUploadProcess> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<FileUploadProcess> findByUploadId(String uploadId) {
            return store.values().stream().filter(j -> j.getUploadId().equals(uploadId)).findFirst();
        }

        @Override
        public Optional<FileUploadProcess> findByUuidFileName(String uuidFileName) {
            return store.values().stream().filter(j -> j.getUuidFileName().equals(uuidFileName)).findFirst();
        }

        @Override
        public List<FileUploadProcess> findProcessableJobs(LocalDateTime now, int batchSize) {
            return store.values().stream().filter(job -> job.canStart(now)).toList();
        }

        @Override
        public List<FileUploadProcess> findLinkableJobs(LocalDateTime now, int batchSize) {
            return store.values().stream().filter(job -> job.canLink(now)).toList();
        }
    }
}
