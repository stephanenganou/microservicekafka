package com.training.libraryeventsconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
public interface LibraryEventService {

    void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;
}
