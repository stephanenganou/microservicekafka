package com.training.kafka_sprint_boot.controller.integration;

import com.training.kafka_sprint_boot.domain.Book;
import com.training.kafka_sprint_boot.domain.LibraryEvent;
import com.training.kafka_sprint_boot.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 1)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class LibraryEventsControllerITest {

    private final static String URL = "/v2/libraryevent";

    private final static String TOPIC = "library-events";

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private TestRestTemplate restTemplate;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }


    @Test
    @Timeout(5)
    void postLibraryEvent() {
        // prepare
        final Book book = Book.builder()
                .bookId(9676)
                .bookAuthor("Greatness Stephane")
                .bookName("From 0 to 1")
                .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(967)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();

        final HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        // test
        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange(URL, HttpMethod.POST, request, LibraryEvent.class);

        // verify
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, TOPIC);
        String expectedRecord = "{\"libraryEventId\":967,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":9676,\"bookName\":\"From 0 to 1\",\"bookAuthor\":\"Greatness Stephane\"}}";
        String consumerRecordValue = consumerRecord.value();
        assertThat(expectedRecord, is(consumerRecordValue));
    }

}