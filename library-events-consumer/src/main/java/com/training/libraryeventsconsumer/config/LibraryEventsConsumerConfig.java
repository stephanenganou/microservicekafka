package com.training.libraryeventsconsumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class LibraryEventsConsumerConfig {

    /**
     * public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
     *                                                                                        ConsumerFactory<Object, Object> kafkaConsumerFactory) {
     *         final ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
     *         configurer.configure(factory, kafkaConsumerFactory);
     *         factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
     *
     *         return factory;
     *     }
     */
}
