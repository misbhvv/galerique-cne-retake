package com.group2.backend.config;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.QueueMessageEncoding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThumbnailQueueConfig {

    @Bean
    public QueueClient thumbnailQueueClient(
        @Value("${app.blob.connection-string}") String connectionString,
        @Value("${app.thumbnail-queue.name}") String queueName
    ) {
        QueueClient client = new QueueClientBuilder()
            .connectionString(connectionString)
            .queueName(queueName)
            .messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient();
        client.createIfNotExists();
        return client;
    }
}
