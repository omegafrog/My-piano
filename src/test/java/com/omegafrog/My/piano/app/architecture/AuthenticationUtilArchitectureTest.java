package com.omegafrog.My.piano.app.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthenticationUtilArchitectureTest {

    private static final Path SERVICE_ROOT = Path.of("src/main/java/com/omegafrog/My/piano/app/web/service");
    private static final String DIRECT_SECURITY_CONTEXT_ACCESS = "SecurityContextHolder";
    private static final String AUTH_UTIL_CALL = "authenticationUtil.getLoggedInUser(";

    @Test
    @DisplayName("서비스 계층은 SecurityContextHolder에 직접 접근하지 않는다")
    void servicesShouldNotAccessSecurityContextHolderDirectly() throws IOException {
        List<Path> directAccessFiles = javaFilesUnder(SERVICE_ROOT)
                .filter(this::containsDirectSecurityContextAccess)
                .sorted(Comparator.naturalOrder())
                .toList();

        assertEquals(List.of(), directAccessFiles,
                () -> "서비스 계층에서 SecurityContextHolder 직접 접근이 발견되었습니다: " + directAccessFiles);
    }

    @Test
    @DisplayName("AuthenticationUtil 사용 범위를 측정한다")
    void measureAuthenticationUtilUsage() throws IOException {
        List<Path> serviceFilesUsingAuthenticationUtil = javaFilesUnder(SERVICE_ROOT)
                .filter(this::containsAuthenticationUtilCall)
                .sorted(Comparator.naturalOrder())
                .toList();

        long usageCount = serviceFilesUsingAuthenticationUtil.stream()
                .mapToLong(this::countAuthenticationUtilCalls)
                .sum();

        System.out.printf(
                "AuthenticationUtil metrics -> serviceFiles=%d, callSites=%d%n",
                serviceFilesUsingAuthenticationUtil.size(),
                usageCount
        );

        assertTrue(serviceFilesUsingAuthenticationUtil.size() > 0,
                "AuthenticationUtil을 사용하는 서비스 파일이 최소 1개 이상이어야 합니다.");
        assertTrue(usageCount > 0,
                "AuthenticationUtil.getLoggedInUser() 호출 지점이 최소 1개 이상이어야 합니다.");
    }

    private Stream<Path> javaFilesUnder(Path root) throws IOException {
        return Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"));
    }

    private boolean containsDirectSecurityContextAccess(Path path) {
        return read(path).contains(DIRECT_SECURITY_CONTEXT_ACCESS);
    }

    private boolean containsAuthenticationUtilCall(Path path) {
        return read(path).contains(AUTH_UTIL_CALL);
    }

    private long countAuthenticationUtilCalls(Path path) {
        return read(path).lines()
                .filter(line -> line.contains(AUTH_UTIL_CALL))
                .count();
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("파일을 읽을 수 없습니다: " + path, e);
        }
    }
}
