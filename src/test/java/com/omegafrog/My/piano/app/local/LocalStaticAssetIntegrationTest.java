package com.omegafrog.My.piano.app.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.omegafrog.My.piano.app.WebConfig;
import com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor;

@SpringJUnitWebConfig(LocalStaticAssetIntegrationTest.TestConfiguration.class)
class LocalStaticAssetIntegrationTest {

    private static final byte[] PROFILE_IMAGE = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    private static final Path STORAGE_ROOT = createStorageRoot();

    private static MockMvc mockMvc;
    private static LocalFileStorageExecutor storageExecutor;
    private static byte[] sheetBytes;

    @BeforeAll
    static void prepareAssets(WebApplicationContext context) throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        storageExecutor = new LocalFileStorageExecutor();
        ReflectionTestUtils.setField(storageExecutor, "basePath", STORAGE_ROOT.toString());
        storageExecutor.init();

        MockMultipartFile profile = new MockMultipartFile(
                "profile", "pianist.png", "image/png", PROFILE_IMAGE);
        storageExecutor.uploadProfileImg(profile, "pianist.png", null);

        Path sourcePdf = STORAGE_ROOT.resolve("evening-score-source.pdf");
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(sourcePdf.toFile());
        }
        sheetBytes = Files.readAllBytes(sourcePdf);
        storageExecutor.uploadSheet(sourcePdf.toFile(), "evening-score.pdf", null);

        PDDocument thumbnailDocument = new PDDocument();
        thumbnailDocument.addPage(new PDPage());
        storageExecutor.uploadThumbnail(thumbnailDocument, "evening-score.pdf", null);
    }

    @DynamicPropertySource
    static void configureStorage(DynamicPropertyRegistry registry) {
        registry.add("local.storage.base-path", STORAGE_ROOT::toString);
    }

    @AfterAll
    static void removeAssets() throws IOException {
        try (var paths = Files.walk(STORAGE_ROOT)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to remove test asset: " + path, exception);
                }
            });
        }
    }

    @Test
    void servesStoredProfileImage() throws Exception {
        mockMvc.perform(get("/profiles/pianist.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PROFILE_IMAGE));
    }

    @Test
    void servesStoredSheetPdfAndKeepsLocalhostUrl() throws Exception {
        assertThat(storageExecutor.buildSheetUrl("evening-score.pdf"))
                .isEqualTo("http://localhost:8080/sheets/evening-score.pdf");

        mockMvc.perform(get("/sheets/evening-score.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(sheetBytes));
    }

    @Test
    void servesGeneratedThumbnailAndKeepsLocalhostUrl() throws Exception {
        assertThat(storageExecutor.buildThumbnailUrls("evening-score.pdf", 1))
                .isEqualTo("http://localhost:8080/thumbnails/evening-score-0.jpg");

        byte[] response = mockMvc.perform(get("/thumbnails/evening-score-0.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(response).isNotEmpty();
    }

    private static Path createStorageRoot() {
        try {
            return Files.createTempDirectory("mypiano-static-assets-");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import(WebConfig.class)
    static class TestConfiguration {
    }
}
