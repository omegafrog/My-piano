package com.omegafrog.My.piano.app.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LocalDeploymentContractTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();

    @Test
    void deployStartsInfrastructureThenWaitsForHealthAndSeed() throws IOException {
        String compose = read("docker-compose.yml");
        String deploy = read("scripts/local-deploy.sh");

        assertThat(compose).contains("spring.profiles.active=local,local-seed");
        assertThat(deploy).contains(
                "set -euo pipefail", "--profile local", "mkdir -p \"$ROOT_DIR/local-storage\"", "compose up -d");
        assertInOrder(deploy,
                "compose up -d",
                "wait_for \"backend health\"",
                "wait_for \"community seed\"",
                "wait_for \"sheet seed\"");
    }

    @Test
    void verificationChecksApisAssetsQualityAndStableCounts() throws IOException {
        String verify = read("scripts/verify-local-deploy.sh");

        assertThat(verify).contains(
                "set -euo pipefail",
                "/healthcheck",
                "/api/v1/community/posts",
                "/api/v1/sheet-post",
                "community-detail.json",
                "sheet-detail.json",
                "for id in \"${community_ids[@]}\"",
                "for id in \"${sheet_ids[@]}\"",
                "/profiles/",
                "/thumbnails/",
                "/sheets/",
                "image/*",
                "application/pdf",
                "dummy|test|sample|fixture",
                "compose restart app-local",
                "community_before",
                "community_after",
                "Backend success is intentionally independent");
        assertThat(verify).doesNotContain("npm ", "My-piano-frontend");
    }

    @Test
    void readmeDocumentsLifecycleEvidenceAndFrontendFallback() throws IOException {
        String readme = read("README.md");

        assertThat(readme).contains(
                "./scripts/local-deploy.sh",
                "./scripts/verify-local-deploy.sh",
                "docker compose --profile local logs --tail=200 app-local",
                "docker compose --profile local restart app-local",
                "docker compose --profile local down -v",
                "프런트엔드는 백엔드 smoke가 성공한 뒤",
                "백엔드 단독 성공 증거");
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(PROJECT_ROOT.resolve(relativePath));
    }

    private void assertInOrder(String content, String... markers) {
        int previous = -1;
        for (String marker : markers) {
            int current = content.indexOf(marker);
            assertThat(current).as("marker %s", marker).isGreaterThan(previous);
            previous = current;
        }
    }
}
