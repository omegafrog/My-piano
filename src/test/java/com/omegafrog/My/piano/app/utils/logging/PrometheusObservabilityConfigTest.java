package com.omegafrog.My.piano.app.utils.logging;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

class PrometheusObservabilityConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            MetricsAutoConfiguration.class,
            CompositeMeterRegistryAutoConfiguration.class,
            PrometheusMetricsExportAutoConfiguration.class))
        .withPropertyValues(
            "management.metrics.tags.application=mypiano",
            "management.endpoint.prometheus.enabled=true");

    @Test
    void prometheusRegistryScrapesSafeUriLabels() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PrometheusMeterRegistry.class);

            PrometheusMeterRegistry registry = context.getBean(PrometheusMeterRegistry.class);
            registry.counter("api_observability_test_requests", "uri", "/api/v1/log-test/{id}")
                .increment();

            String scrape = registry.scrape();
            assertThat(scrape)
                .contains("api_observability_test_requests_total")
                .contains("uri=\"/api/v1/log-test/{id}\"")
                .doesNotContain("/api/v1/log-test/42");
        });
    }

    @Test
    void applicationYamlExposesPrometheusEndpoint() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(
            "application", new FileSystemResource("src/main/resources/application.yml"));

        assertThat(sources)
            .extracting(source -> source.getProperty("management.endpoints.web.exposure.include"))
            .anySatisfy(value -> assertThat(String.valueOf(value)).contains("prometheus"));
    }
}
