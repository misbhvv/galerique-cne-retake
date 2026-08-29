package com.group2.backend.service;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.backend.dto.ThumbnailJobMessage;
import com.group2.backend.exception.service.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThumbnailQueueServiceTest {

    private final QueueClient queueClient = mock(QueueClient.class);
    private final ThumbnailQueueService service = new ThumbnailQueueService(queueClient, new ObjectMapper());

    @Test
    void sendsThumbnailJobAsJson() throws Exception {
        ThumbnailJobMessage job = new ThumbnailJobMessage(
            "artworks/123/original.jpg",
            "artworks/123/thumbnails/original.jpg",
            300
        );

        service.enqueue(job);

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(queueClient).sendMessage(payload.capture());
        ThumbnailJobMessage sent = new ObjectMapper().readValue(payload.getValue(), ThumbnailJobMessage.class);
        assertEquals(job, sent);
    }

    @Test
    void returnsServiceUnavailableWhenQueueCannotBeReached() {
        when(queueClient.sendMessage(org.mockito.ArgumentMatchers.anyString()))
            .thenThrow(new RuntimeException("queue unavailable"));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> service.enqueue(new ThumbnailJobMessage("source", "thumbnail", 300))
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }
}
