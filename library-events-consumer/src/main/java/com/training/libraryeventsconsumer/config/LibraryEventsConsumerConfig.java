package com.training.libraryeventsconsumer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
                                                                                       ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);

        // To set the Acknowledgment of Events manually
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        //factory.setConcurrency(1);
        factory.setCommonErrorHandler(createErrorHandler());

        return factory;
    }

    public DefaultErrorHandler createErrorHandler() {
        // with delay of one second
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(fixedBackOff);
        getExceptionsToIgnoreList().forEach(defaultErrorHandler::addNotRetryableExceptions);
        defaultErrorHandler.setRetryListeners(((consumerRecord, error, deliverAttempt) -> {
            log.info("Failed Record in Retry Listener, Exception: {}, deliveryAttempt: {}", error.getMessage(), deliverAttempt);
        }));

        return defaultErrorHandler;
    }

    private List getExceptionsToIgnoreList() {

        return List.of(
                IllegalArgumentException.class
        );
    }

}
