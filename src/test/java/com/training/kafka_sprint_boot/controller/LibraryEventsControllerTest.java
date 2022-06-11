package com.training.kafka_sprint_boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.kafka_sprint_boot.domain.Book;
import com.training.kafka_sprint_boot.domain.LibraryEvent;
import com.training.kafka_sprint_boot.domain.LibraryEventType;
import com.training.kafka_sprint_boot.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
class LibraryEventsControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String URL = "/v1/libraryevent";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LibraryEventProducer libraryEventProducerMock;


    @Test
    void postLibraryEvent_withValidData_thenReturnStatusCreated() throws Exception {

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

        String json = objectMapper.writeValueAsString(libraryEvent);

        doNothing().when(libraryEventProducerMock).sendLibraryEvent(isA(LibraryEvent.class));

        // test and verify
        mockMvc.perform(post(URL)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
    }

    @Test
    void postLibraryEvent_withInvalidBookData_thenReturnStatusBadRequest() throws Exception {

        // prepare
        final Book book = Book.builder()
                .bookId(9676)
                .bookAuthor("")
                .bookName("From 0 to 1")
                .build();

        final LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(967)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        doNothing().when(libraryEventProducerMock).sendLibraryEvent(isA(LibraryEvent.class));

        // test and verify
        mockMvc.perform(post(URL)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void postLibraryEvent_withInvalidLibraryEventData_thenReturnStatusBadRequest() throws Exception {

        // prepare
        final LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(967)
                .libraryEventType(LibraryEventType.NEW)
                .book(null)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        doNothing().when(libraryEventProducerMock).sendLibraryEvent(isA(LibraryEvent.class));

        // test and verify
        mockMvc.perform(post(URL)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }
}