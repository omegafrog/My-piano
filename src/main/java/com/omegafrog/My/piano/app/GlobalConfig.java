package com.omegafrog.My.piano.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.external.tossPayment.TossPaymentInstance;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.notification.PushInstance;
import com.omegafrog.My.piano.app.web.domain.order.SellableItemFactory;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import io.awspring.cloud.s3.InMemoryBufferingS3OutputStreamProvider;
import io.awspring.cloud.s3.Jackson2JsonS3ObjectConverter;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;

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

    @Value("${firebase.app.admin.json}")
    private String serviceAccountPath;


    @Bean
    public SellableItemFactory sellableItemFactory(LessonRepository lessonRepository, SheetPostRepository sheetPostRepository) {
        return new SellableItemFactory(lessonRepository, sheetPostRepository);
    }

    @Bean
    public PushInstance pushInstance() throws IOException {
        return new PushInstance(serviceAccountPath);
    }

    @Bean
    public MapperUtil mapperUtil(ObjectMapper objectMapper){
        return new MapperUtil(objectMapper);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }



    @Bean
    public ElasticSearchInstance elasticSearchInstance() {
        return new ElasticSearchInstance();
    }

    @Bean
    public TossPaymentInstance tossPaymentInstance(RestTemplate restTemplate, MapperUtil mapperUtil) {
        return new TossPaymentInstance(restTemplate, mapperUtil);
    }

    @Bean
    public S3UploadFileExecutor s3UploadFileExecutor() {
        return new S3UploadFileExecutor(s3Template());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        accessToken, secretToken
                )))
                .build();
    }

    @Bean
    public S3Template s3Template() {
        return new S3Template(s3Client(), new InMemoryBufferingS3OutputStreamProvider(s3Client(), null),
                new Jackson2JsonS3ObjectConverter(objectMapper()), S3Presigner.create());
    }


    @Bean
    public GooglePublicKeysManager googlePublicKeysManager() {
        return new GooglePublicKeysManager(new ApacheHttpTransport(), GsonFactory.getDefaultInstance());
    }

}
