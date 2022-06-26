package com.training.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.libraryeventsconsumer.entity.Book;
import com.training.libraryeventsconsumer.entity.LibraryEvent;
import com.training.libraryeventsconsumer.repository.LibraryEventRepository;
import com.training.libraryeventsconsumer.service.LibraryEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 1)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class LibraryEventsConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    private LibraryEventRepository libraryEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private LibraryEventService libraryEventServiceSpy;

    @SpyBean
    private LibraryEventsConsumer libraryEventsConsumerSpy;

    private CountDownLatch countDownLatch;


    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
        countDownLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        libraryEventRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        // Prepare
        final String jsonString = "{\n    \"libraryEventId\": 967,\n    \"libraryEventType\": \"NEW\",\n    \"book\": {\n        \"bookId\": 967,\n        \"bookName\": \"Kafka Using Spring Boot 2\",\n        \"bookAuthor\": \"Greatness Stephane\"\n    }\n}";

        // Test
        // making the call asynchronous
        kafkaTemplate.sendDefault(jsonString).get();

        countDownLatch.await(3, TimeUnit.SECONDS);

        // Verify
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventRepository.findAll();
        assertThat(libraryEventList.size(), is(1));
        libraryEventList.forEach(libraryEvent -> {
            assert libraryEvent.getLibraryEventId() != null;
            assertThat(libraryEvent.getBook().getBookId(), is(967));
        });
    }

    @Test
    @Disabled
    void publishUpdateLibraryEvent() throws JsonProcessingException, InterruptedException {
        // Prepare
        final String jsonString = "{\n    \"libraryEventId\": 967,\n    \"libraryEventType\": \"NEW\",\n    \"book\": {\n        \"bookId\": 967,\n        \"bookName\": \"Kafka Using Spring Boot 2\",\n        \"bookAuthor\": \"Greatness Stephane\"\n    }\n}";
        final LibraryEvent libraryEvent = objectMapper.readValue(jsonString, LibraryEvent.class);
        libraryEvent.getBook().setLibraryEvent(libraryEvent);

        libraryEventRepository.save(libraryEvent);
        String updatedBookName = "Kafka Using Spring Boot 2.x";
        Book updatedBook = Book.builder()
                .bookId(libraryEvent.getBook().getBookId())
                .bookName(updatedBookName)
                .bookAuthor(libraryEvent.getBook().getBookAuthor())
                .build();
        libraryEvent.setBook(updatedBook);

        // Test
        final String updatedJsonString = objectMapper.writeValueAsString(libraryEvent);
        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJsonString);

        countDownLatch.await(3, TimeUnit.SECONDS);

        // Verify
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
        //final LibraryEvent persistedLibraryEvent = libraryEventRepository.findById(1).get();
        //assertThat(persistedLibraryEvent.getBook().getBookName(), is(updatedBookName));

    }
}