package com.omegafrog.My.piano.app;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:8.0.36");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2.4-alpine");
    private static final DockerImageName ELASTICSEARCH_IMAGE = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.0");
    private static final DockerImageName KIBANA_IMAGE = DockerImageName.parse("docker.elastic.co/kibana/kibana:8.15.0");

    private static final boolean ENABLE_ELASTICSEARCH = flag("mypiano.tc.elasticsearch.enabled", "MYPIANO_TC_ELASTICSEARCH_ENABLED", false);
    private static final boolean ENABLE_KIBANA = flag("mypiano.tc.kibana.enabled", "MYPIANO_TC_KIBANA_ENABLED", false);
    private static final boolean CONTAINERS_REQUIRED = flag("mypiano.tc.required", "MYPIANO_TC_REQUIRED", true);

    private static final Network SEARCH_NETWORK = Network.newNetwork();

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(MYSQL_IMAGE)
            .withDatabaseName("mypiano")
            .withUsername("test")
            .withPassword("test");

    private static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    private static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withNetwork(SEARCH_NETWORK)
            .withNetworkAliases("elasticsearch-test")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(5));

    private static final GenericContainer<?> KIBANA = new GenericContainer<>(KIBANA_IMAGE)
            .dependsOn(ELASTICSEARCH)
            .withNetwork(SEARCH_NETWORK)
            .withExposedPorts(5601)
            .withEnv("SERVER_HOST", "0.0.0.0")
            .withEnv("ELASTICSEARCH_HOSTS", "http://elasticsearch-test:9200");

    private static volatile boolean initialized = false;
    private static volatile boolean containersAvailable = false;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ensureStarted();

        if (!containersAvailable) {
            throw new IllegalStateException("Docker is required for Testcontainers but could not be started. Check the Docker host configuration.");
        }

        List<String> properties = new ArrayList<>();
        properties.add("spring.datasource.url=" + MYSQL.getJdbcUrl());
        properties.add("spring.datasource.username=" + MYSQL.getUsername());
        properties.add("spring.datasource.password=" + MYSQL.getPassword());
        properties.add("spring.datasource.driver-class-name=" + MYSQL.getDriverClassName());

        String redisHost = REDIS.getHost();
        Integer redisPort = REDIS.getMappedPort(6379);
        properties.add("spring.redis.user.host=" + redisHost);
        properties.add("spring.redis.user.port=" + redisPort);
        properties.add("spring.redis.cache.host=" + redisHost);
        properties.add("spring.redis.cache.port=" + redisPort);
        properties.add("spring.data.redis.host=" + redisHost);
        properties.add("spring.data.redis.port=" + redisPort);

        if (ENABLE_ELASTICSEARCH) {
            String esHost = ELASTICSEARCH.getHost();
            Integer esPort = ELASTICSEARCH.getMappedPort(9200);
            properties.add("elasticsearch.host=" + esHost);
            properties.add("elasticsearch.port=" + esPort);
            properties.add("spring.elasticsearch.uris=http://" + esHost + ":" + esPort);
        }

        if (ENABLE_KIBANA) {
            properties.add("kibana.host=" + KIBANA.getHost());
            properties.add("kibana.port=" + KIBANA.getMappedPort(5601));
        }

        TestPropertyValues.of(properties).applyTo(applicationContext);
    }

    private static synchronized void ensureStarted() {
        if (initialized) {
            return;
        }

        initialized = true;

        Stream<? extends Startable> targets = ENABLE_ELASTICSEARCH
                ? (ENABLE_KIBANA ? Stream.of(MYSQL, REDIS, ELASTICSEARCH, KIBANA) : Stream.of(MYSQL, REDIS, ELASTICSEARCH))
                : Stream.of(MYSQL, REDIS);

        try {
            Startables.deepStart(targets).join();
            containersAvailable = true;
        } catch (RuntimeException ex) {
            containersAvailable = false;
            if (CONTAINERS_REQUIRED) {
                throw ex;
            }
        }
    }

    private static boolean flag(String systemPropertyKey, String environmentKey, boolean defaultValue) {
        String propertyValue = System.getProperty(systemPropertyKey);
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }
        String envValue = System.getenv(environmentKey);
        if (envValue != null) {
            return Boolean.parseBoolean(envValue);
        }
        return defaultValue;
    }

}
