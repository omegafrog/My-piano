package com.omegafrog.My.piano.app.web.event;

import com.omegafrog.My.piano.app.web.service.FileUploadService;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@Profile("test")
public class TestEventPublisherConfig {

    @Bean
    public EventPublisher eventPublisher(ObjectProvider<FileUploadService> fileUploadServiceProvider) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate = (KafkaTemplate<String, Object>) Mockito.mock(KafkaTemplate.class);

        Mockito.doAnswer(invocation -> {
            String topic = invocation.getArgument(0);
            Object payload = invocation.getArgument(2);

            if ("file-upload-completed-topic".equals(topic) && payload instanceof FileUploadCompletedEvent event) {
                FileUploadService fileUploadService = fileUploadServiceProvider.getIfAvailable();
                if (fileUploadService == null) {
                    return null;
                }
                fileUploadService.updateUploadData(
                        event.getUploadId(),
                        event.getSheetUrl(),
                        event.getThumbnailUrl(),
                        event.getPageNum());
            }
            return null;
        }).when(kafkaTemplate).send(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        return new EventPublisher(kafkaTemplate);
    }
}
