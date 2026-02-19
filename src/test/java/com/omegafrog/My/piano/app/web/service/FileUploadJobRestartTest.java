package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagedPdf;
import com.omegafrog.My.piano.app.web.infra.fileUpload.LocalStagePdfStorage;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import io.awspring.cloud.s3.ObjectMetadata;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
class FileUploadJobRestartTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void failedUploadJobCanBeRetriedAfterRestartUsingStagedPdf() throws Exception {
        String uploadId = "upload-restart-1";
        byte[] pdfBytes = createMinimalPdfBytes(1);

        StagePdfStorage stagePdfStorage = new LocalStagePdfStorage(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "score.pdf", "application/pdf", pdfBytes);
        StagedPdf staged = stagePdfStorage.stage(file, uploadId);

        InMemoryFileUploadJobRepository repo = new InMemoryFileUploadJobRepository();
        FileUploadJob job = FileUploadJob.builder()
                .uploadId(uploadId)
                .originalFileName("score.pdf")
                .uuidFileName("uuid-score.pdf")
                .stagedFilePath(staged.stagePath())
                .status(FileUploadJobStatus.PENDING)
                .nextAttemptAt(LocalDateTime.now().minusSeconds(1))
                .maxAttempts(3)
                .build();
        job = repo.save(job);

        UploadFileExecutor uploadFileExecutor = new FailOnceUploadFileExecutor();
        FileStorageExecutor fileStorageExecutor = new FileStorageExecutor(uploadFileExecutor);

        FileUploadRedisReadModelWriter readModelWriter = Mockito.mock(FileUploadRedisReadModelWriter.class);
        TransactionTemplate tx = new TransactionTemplate(new NoopTransactionManager());

        // First run: fail mid-way (thumbnail upload fails) -> RETRY, staged file remains
        FileUploadJobScheduler scheduler1 = new FileUploadJobScheduler(repo, fileStorageExecutor, stagePdfStorage, readModelWriter, tx);
        ReflectionTestUtils.setField(scheduler1, "batchSize", 5);
        ReflectionTestUtils.setField(scheduler1, "retryDelaySeconds", 5);

        scheduler1.processPendingJobs();

        FileUploadJob afterFail = repo.findById(job.getId()).orElseThrow();
        assertThat(afterFail.getStatus()).isEqualTo(FileUploadJobStatus.RETRY);
        assertThat(new File(staged.stagePath())).exists();

        // Simulate time passing / restart: make job eligible again
        ReflectionTestUtils.setField(afterFail, "nextAttemptAt", LocalDateTime.now().minusSeconds(1));
        repo.save(afterFail);

        // Second run (new instance): succeeds -> COMPLETED and staged file deleted
        FileUploadJobScheduler scheduler2 = new FileUploadJobScheduler(repo, fileStorageExecutor, stagePdfStorage, readModelWriter, tx);
        ReflectionTestUtils.setField(scheduler2, "batchSize", 5);
        ReflectionTestUtils.setField(scheduler2, "retryDelaySeconds", 5);

        scheduler2.processPendingJobs();

        FileUploadJob afterSuccess = repo.findById(job.getId()).orElseThrow();
        assertThat(afterSuccess.getStatus()).isEqualTo(FileUploadJobStatus.COMPLETED);
        assertThat(new File(staged.stagePath())).doesNotExist();
    }

    private static class NoopTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }

    private static byte[] createMinimalPdfBytes(int pageCount) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < pageCount; i++) {
                doc.addPage(new PDPage());
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static class FailOnceUploadFileExecutor implements UploadFileExecutor {
        private final AtomicInteger thumbnailAttempts = new AtomicInteger(0);

        @Override
        public void uploadSheet(File file, String filename, ObjectMetadata metadata) {
        }

        @Override
        public void uploadThumbnail(PDDocument document, String filename, ObjectMetadata metadata) throws java.io.FileNotFoundException {
            if (thumbnailAttempts.incrementAndGet() == 1) {
                throw new java.io.FileNotFoundException("Simulated thumbnail upload failure");
            }
        }

        @Override
        public void uploadProfileImg(org.springframework.web.multipart.MultipartFile profileImg, String filename, ObjectMetadata metadata) {
        }

        @Override
        public void removeProfileImg(String url) {
        }

        @Override
        public void removeSheetPost(com.omegafrog.My.piano.app.web.domain.sheet.SheetPost sheetPost) {
        }

        @Override
        public URL createFileUrl(String sheetUrl) {
            return null;
        }

        @Override
        public String buildSheetUrl(String filename) {
            return "sheetUrl";
        }

        @Override
        public String buildThumbnailUrls(String filename, int pageNum) {
            return "thumb1,thumb2";
        }

        @Override
        public void uploadSheetAsync(File file, String filename, ObjectMetadata metadata, String uploadId) {
        }

        @Override
        public void uploadThumbnailAsync(PDDocument document, String filename, ObjectMetadata metadata, String uploadId) {
        }
    }

    private static class InMemoryFileUploadJobRepository implements FileUploadJobRepository {
        private final AtomicLong idSeq = new AtomicLong(0);
        private final Map<Long, FileUploadJob> store = new HashMap<>();

        @Override
        public FileUploadJob save(FileUploadJob job) {
            if (job.getId() == null) {
                Long id = idSeq.incrementAndGet();
                ReflectionTestUtils.setField(job, "id", id);
                ReflectionTestUtils.setField(job, "createdAt", LocalDateTime.now());
                ReflectionTestUtils.setField(job, "updatedAt", LocalDateTime.now());
            } else {
                ReflectionTestUtils.setField(job, "updatedAt", LocalDateTime.now());
            }
            store.put(job.getId(), job);
            return job;
        }

        @Override
        public Optional<FileUploadJob> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<FileUploadJob> findProcessableJobs(LocalDateTime now, int batchSize) {
            List<FileUploadJob> candidates = new ArrayList<>();
            for (FileUploadJob job : store.values()) {
                if ((job.getStatus() == FileUploadJobStatus.PENDING || job.getStatus() == FileUploadJobStatus.RETRY)
                        && !job.getNextAttemptAt().isAfter(now)) {
                    candidates.add(job);
                }
            }
            candidates.sort(Comparator.comparing(FileUploadJob::getId));
            if (candidates.size() <= batchSize) {
                return candidates;
            }
            return candidates.subList(0, Math.max(1, batchSize));
        }

        @Override
        public Optional<FileUploadJob> findByUploadId(String uploadId) {
            return store.values().stream().filter(job -> job.getUploadId().equals(uploadId)).findFirst();
        }

        @Override
        public List<FileUploadJob> findLinkableJobs(LocalDateTime now, int batchSize) {
            return List.of();
        }
    }
}
