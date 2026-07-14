package com.omegafrog.My.piano.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.BackOffExecution;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CouponKafkaConfigTest {

    private final CouponKafkaConfig config = new CouponKafkaConfig();

    @Test
    void configuresJsonProducerAndConsumerForCouponPaymentResults() {
        KafkaProperties properties = new KafkaProperties();
        properties.setBootstrapServers(List.of("localhost:9092"));
        properties.getConsumer().setGroupId("coupon-payment-result-consumer");

        Map<String, Object> producerProperties = config.couponProducerFactory(properties).getConfigurationProperties();
        Map<String, Object> consumerProperties = config.couponConsumerFactory(properties).getConfigurationProperties();

        assertThat(producerProperties)
                .containsEntry("key.serializer", StringSerializer.class)
                .containsEntry("value.serializer", JsonSerializer.class);
        assertThat(consumerProperties)
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "coupon-payment-result-consumer")
                .containsEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
                .containsEntry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class)
                .containsEntry(JsonDeserializer.TRUSTED_PACKAGES, "com.omegafrog.My.piano.app.web.event.payment");
    }

    @Test
    void createsPaymentResultTopicsAndRetriesThreeTimesBeforeDlt() {
        assertThat(config.paymentSucceededTopic("payment-succeeded").name()).isEqualTo("payment-succeeded");
        assertThat(config.paymentFailedTopic("payment-failed").name()).isEqualTo("payment-failed");

        ExponentialBackOffWithMaxRetries backOff = config.couponRetryBackOff();
        BackOffExecution execution = backOff.start();

        assertThat(execution.nextBackOff()).isEqualTo(1_000L);
        assertThat(execution.nextBackOff()).isEqualTo(2_000L);
        assertThat(execution.nextBackOff()).isEqualTo(4_000L);
        assertThat(execution.nextBackOff()).isEqualTo(BackOffExecution.STOP);

        TopicPartition destination = CouponKafkaConfig.deadLetterTopic(new ConsumerRecord<>("payment-failed", 2, 0L, "key", "value"));
        assertThat(destination.topic()).isEqualTo("payment-failed.DLT");
        assertThat(destination.partition()).isEqualTo(2);
    }
}
