package com.training.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.libraryeventsconsumer.entity.LibraryEvent;
import com.training.libraryeventsconsumer.repository.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventServiceImp implements LibraryEventService {

    private final ObjectMapper objectMapper;

    private final LibraryEventRepository libraryEventRepository;

    @Autowired
    public LibraryEventServiceImp(final ObjectMapper objectMapper, final LibraryEventRepository libraryEventRepository) {
        this.objectMapper = objectMapper;
        this.libraryEventRepository = libraryEventRepository;
    }

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        final LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("LibraryEvent: {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                save(libraryEvent);
                break;

            case UPDATE:
                valideEvent(libraryEvent);
                save(libraryEvent);
                break;

            default:
                log.info("Invalid Library Event Type");
        }
    }

    private void save(final LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully Persisted the library Event {}", libraryEvent);
    }

    private void valideEvent(LibraryEvent libraryEvent) {
        if (null == libraryEvent.getLibraryEventId()) {
            throw new IllegalArgumentException("Library Event Id is missing.");
        }

        Optional<LibraryEvent> persistedLibraryEvent = libraryEventRepository.findById(libraryEvent.getLibraryEventId());
        if (persistedLibraryEvent.isEmpty()) {
            throw new IllegalArgumentException("Library Event Id is missing.");
        }
        log.info("Validation is successful for the library Event: {}", persistedLibraryEvent.get());
    }
}
