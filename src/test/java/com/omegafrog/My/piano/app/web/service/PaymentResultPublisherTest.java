package com.omegafrog.My.piano.app.web.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import com.omegafrog.My.piano.app.web.event.payment.PaymentResultPublisher;
import org.mockito.Mockito;

class PaymentResultPublisherTest {
    @Test void successPublishesToConfiguredTopic() {
        @SuppressWarnings("unchecked") KafkaTemplate<String, Object> template = Mockito.mock(KafkaTemplate.class);
        Mockito.when(template.send(any(String.class), any(String.class), any())).thenReturn(CompletableFuture.completedFuture(null));
        PaymentResultPublisher publisher = new PaymentResultPublisher(template);
        org.springframework.test.util.ReflectionTestUtils.setField(publisher, "succeededTopic", "payment-succeeded");
        publisher.publishSucceeded("1");
        verify(template).send(eq("payment-succeeded"), eq("1"), any());
    }
}
