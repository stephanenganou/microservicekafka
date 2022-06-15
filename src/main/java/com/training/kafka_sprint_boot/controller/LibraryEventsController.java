package com.training.kafka_sprint_boot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.training.kafka_sprint_boot.domain.LibraryEvent;
import com.training.kafka_sprint_boot.domain.LibraryEventType;
import com.training.kafka_sprint_boot.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    private final LibraryEventProducer libraryEventProducer;

    @Autowired
    public LibraryEventsController(final LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid final LibraryEvent libraryEvent) {
        //invoke kafka producer
        try {
            libraryEventProducer.sendLibraryEvent(libraryEvent);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(libraryEvent);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(libraryEvent);
    }

    @PostMapping("/v1/libraryevent/sync")
    public ResponseEntity<LibraryEvent> postLibrarySync(@RequestBody @Valid final LibraryEvent libraryEvent) {
        //invoke kafka producer
        try {
            libraryEvent.setLibraryEventType(LibraryEventType.NEW);
            SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSync(libraryEvent);
            log.info("SendResult is {}", sendResult.toString());
        } catch (JsonProcessingException | ExecutionException | InterruptedException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(libraryEvent);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(libraryEvent);
    }

    @PostMapping("/v2/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent2(@RequestBody @Valid final LibraryEvent libraryEvent) {
        //invoke kafka producer
        try {
            libraryEvent.setLibraryEventType(LibraryEventType.NEW);
            libraryEventProducer.sendLibraryEvent_approach2(libraryEvent);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(libraryEvent);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid final LibraryEvent libraryEvent) {

        if (null == libraryEvent.getLibraryEventId())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("please pass the LibraryEventId");

        //invoke kafka producer
        try {
            libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
            libraryEventProducer.sendLibraryEvent_approach2(libraryEvent);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(libraryEvent);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(libraryEvent);
    }
}
