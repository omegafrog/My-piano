package com.omegafrog.My.piano.app.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.yaml.snakeyaml.Yaml;

class LocalProfileConfigurationTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();

    @Test
    void localProfileLoadsContainerConfigurationWithoutSecretProfile() {
        StandardEnvironment environment = loadEnvironment("local");

        assertThat(environment.getActiveProfiles()).containsExactly("local");
        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:mysql://mysql-mypiano:3306/mypiano?characterEncoding=UTF-8&serverTimezone=UTC");
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create");
        assertThat(environment.getProperty("spring.redis.user.host")).isEqualTo("redis-mypiano-user");
        assertThat(environment.getProperty("spring.redis.user.port", Integer.class)).isEqualTo(6379);
        assertThat(environment.getProperty("spring.redis.cache.host")).isEqualTo("redis-mypiano-cache");
        assertThat(environment.getProperty("spring.redis.cache.port", Integer.class)).isEqualTo(6379);
        assertThat(environment.getProperty("elasticsearch.host")).isEqualTo("elasticsearch");
        assertThat(environment.getProperty("elasticsearch.port", Integer.class)).isEqualTo(9200);
        assertThat(environment.getProperty("local.storage.base-path")).isEqualTo("/workspace/local-storage");
    }

    @Test
    void prodProfileKeepsExistingConfigurationAndIncludesSecretProfile() {
        StandardEnvironment environment = loadEnvironment("prod");

        assertThat(environment.getActiveProfiles()).containsExactlyInAnyOrder("prod", "secret");
        assertThat(environment.getActiveProfiles()).doesNotContain("local");
        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:mysql://172.31.36.227:3306/mypianotest?characterEncoding=UTF-8&serverTimezone=UTC");
        assertThat(environment.getProperty("server.ssl.enabled", Boolean.class)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void composeUsesMatchingDatabasePortsAndHealthyDependencies() throws IOException {
        Map<String, Object> compose = new Yaml().load(Files.readString(PROJECT_ROOT.resolve("docker-compose.yml")));
        Map<String, Object> services = (Map<String, Object>) compose.get("services");
        Map<String, Object> mysql = (Map<String, Object>) services.get("mysql-mypiano");
        Map<String, Object> app = (Map<String, Object>) services.get("app-local");

        assertThat((List<String>) mysql.get("environment")).contains("MYSQL_DATABASE=mypiano");
        assertThat((List<String>) mysql.get("ports")).contains("3307:3306");
        assertThat((List<String>) app.get("profiles")).containsExactly("local");
        assertThat((List<String>) app.get("ports")).contains("8080:8080");
        assertThat((List<String>) app.get("command")).anyMatch(value -> value.contains("spring.profiles.active=local"));

        Map<String, Object> dependencies = (Map<String, Object>) app.get("depends_on");
        assertThat(dependencies).containsOnlyKeys(
                "mysql-mypiano", "elasticsearch", "redis-mypiano-user", "redis-mypiano-cache");
        dependencies.values().forEach(value ->
                assertThat((Map<String, Object>) value).containsEntry("condition", "service_healthy"));

        String initSql = Files.readString(PROJECT_ROOT.resolve("docker/mysql/init/01-create-test-database.sql"));
        assertThat(initSql).containsIgnoringCase("CREATE DATABASE IF NOT EXISTS mypianotest");
    }

    private StandardEnvironment loadEnvironment(String profile) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
                "test-profile", Map.of(
                        "spring.profiles.active", profile,
                        "spring.config.location", "file:./src/main/resources/")));
        ConfigDataEnvironmentPostProcessor.applyTo(environment);
        assertThat(Arrays.asList(environment.getActiveProfiles())).doesNotContain("dev");
        return environment;
    }
}
