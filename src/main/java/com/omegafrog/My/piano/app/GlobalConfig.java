package com.omegafrog.My.piano.app;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.tossPayment.TossPaymentInstance;
import com.omegafrog.My.piano.app.external.tossPayment.TossWebHookResultFactory;
import com.omegafrog.My.piano.app.external.tossPayment.TossWebHookResultFactoryImpl;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.OrderDtoCustomSerializer;
import com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.notification.PushInstance;
import com.omegafrog.My.piano.app.web.domain.order.SellableItemFactory;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.event.EventPublisher;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import io.awspring.cloud.s3.InMemoryBufferingS3OutputStreamProvider;
import io.awspring.cloud.s3.Jackson2JsonS3ObjectConverter;
import io.awspring.cloud.s3.S3Template;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class GlobalConfig {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessToken;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretToken;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private String port;
    @Value("${elasticsearch.apiKey}")
    private String apiKey;
    @Value("${firebase.app.admin.json}")
    private String serviceAccountPath;

    @Bean
    public SellableItemFactory sellableItemFactory(LessonRepository lessonRepository,
            SheetPostRepository sheetPostRepository) {
        return new SellableItemFactory(lessonRepository, sheetPostRepository);
    }

    @Bean
    public PushInstance pushInstance() throws IOException {
        System.out.println("serviceAccountPath = " + serviceAccountPath);
        return new PushInstance(serviceAccountPath);
    }

    @Bean
    public MapperUtil mapperUtil(ObjectMapper objectMapper, AuthenticationUtil authenticationUtil) {
        return new MapperUtil(objectMapper, authenticationUtil);
    }

    public ElasticsearchClient elasticsearchClient() {
        String serverUrl = "https://" + host + ":" + port;

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[] {
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    @Bean
    public TossWebHookResultFactory tossWebHookResultFactory(ObjectMapper objectMapper) {
        return new TossWebHookResultFactoryImpl(objectMapper);
    }

    static final int READ_TIMEOUT = 1500;
    static final int CONN_TIMEOUT = 3000;

    @Bean
    public RestTemplate restTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(createHttpClient());
        return new RestTemplate(factory);
    }

    private CloseableHttpClient createHttpClient() {
        return org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create()
                .setConnectionManager(createHttpClientConnectionManager())
                .build();
    }

    private HttpClientConnectionManager createHttpClientConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                        .setConnectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS)
                        .build())
                .build();
    }

    @Bean
    public TossPaymentInstance tossPaymentInstance(RestTemplate restTemplate, MapperUtil mapperUtil) {
        return new TossPaymentInstance(restTemplate, mapperUtil);
    }

    @Bean
    @Profile("prod")
    public UploadFileExecutor s3UploadFileExecutor(EventPublisher eventPublisher) {
        return new S3UploadFileExecutor(s3Template(), s3Client(), eventPublisher);
    }

    @Bean
    public com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor localFileStorageExecutor() {
        return new com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor();
    }

    @Bean
    public com.omegafrog.My.piano.app.web.domain.FileStorageExecutor fileStorageExecutor(
            UploadFileExecutor uploadFileExecutor) {
        return new com.omegafrog.My.piano.app.web.domain.FileStorageExecutor(uploadFileExecutor);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OrderDto.class, new OrderDtoCustomSerializer()));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    @Bean
    @Profile("prod")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        accessToken, secretToken)))
                .build();
    }

    @Bean
    @Profile("prod")
    public S3Template s3Template() {
        return new S3Template(s3Client(), new InMemoryBufferingS3OutputStreamProvider(s3Client(), null),
                new Jackson2JsonS3ObjectConverter(objectMapper()), S3Presigner.create());
    }

    @Bean
    public GooglePublicKeysManager googlePublicKeysManager() {
        return new GooglePublicKeysManager(new ApacheHttpTransport(), GsonFactory.getDefaultInstance());
    }

}
