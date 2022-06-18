package com.training.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.training.libraryeventsconsumer.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsConsumer {

    private final LibraryEventService libraryEventService;

    @Autowired
    public LibraryEventsConsumer(LibraryEventService libraryEventService) {
        this.libraryEventService = libraryEventService;
    }

    @KafkaListener(topics = {"library-events"})
    public void onMessage(final ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Received ConsumerRecord: {}", consumerRecord);

        libraryEventService.processLibraryEvent(consumerRecord);
    }
}
