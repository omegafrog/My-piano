package com.omegafrog.My.piano.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CouponKafkaConfig {

    static final int RETRY_COUNT = 3;
    static final long INITIAL_RETRY_INTERVAL_MILLIS = 1_000L;
    static final double RETRY_MULTIPLIER = 2.0;

    @Bean
    public org.apache.kafka.clients.admin.NewTopic paymentSucceededTopic(
            @Value("${coupon.kafka.payment-succeeded-topic:payment-succeeded}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    @Bean
    public org.apache.kafka.clients.admin.NewTopic paymentFailedTopic(
            @Value("${coupon.kafka.payment-failed-topic:payment-failed}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, Object> couponProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        properties.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, Object> couponKafkaTemplate(ProducerFactory<String, Object> couponProducerFactory) {
        return new KafkaTemplate<>(couponProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> couponConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.omegafrog.My.piano.app.web.event.payment");
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), new JsonDeserializer<>());
    }

    @Bean
    public ExponentialBackOffWithMaxRetries couponRetryBackOff() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(RETRY_COUNT);
        backOff.setInitialInterval(INITIAL_RETRY_INTERVAL_MILLIS);
        backOff.setMultiplier(RETRY_MULTIPLIER);
        return backOff;
    }

    @Bean
    public DefaultErrorHandler couponKafkaErrorHandler(
            KafkaTemplate<String, Object> couponKafkaTemplate,
            ExponentialBackOffWithMaxRetries couponRetryBackOff
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                couponKafkaTemplate,
                (record, exception) -> deadLetterTopic(record)
        );
        return new DefaultErrorHandler(recoverer, couponRetryBackOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> couponKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> couponConsumerFactory,
            DefaultErrorHandler couponKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(couponConsumerFactory);
        factory.setCommonErrorHandler(couponKafkaErrorHandler);
        return factory;
    }

    static TopicPartition deadLetterTopic(ConsumerRecord<?, ?> record) {
        return new TopicPartition(record.topic() + ".DLT", record.partition());
    }
}
