package com.group2.backend.service;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.backend.dto.ThumbnailJobMessage;
import com.group2.backend.exception.service.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailQueueService {

    private final QueueClient thumbnailQueueClient;
    private final ObjectMapper objectMapper;

    public void enqueue(ThumbnailJobMessage job) {
        try {
            String payload = objectMapper.writeValueAsString(job);
            thumbnailQueueClient.sendMessage(payload);
            log.info("Queued thumbnail job for {}", job.sourceBlobName());
        } catch (JsonProcessingException ex) {
            throw new ServiceException("Failed to create thumbnail job", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException ex) {
            log.error("Failed to queue thumbnail job for {}", job.sourceBlobName(), ex);
            throw new ServiceException("Thumbnail processing is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
