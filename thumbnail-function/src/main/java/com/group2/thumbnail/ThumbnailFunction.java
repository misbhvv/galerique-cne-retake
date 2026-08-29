package com.group2.thumbnail;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ThumbnailFunction {

    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();

    @FunctionName("generateThumbnail")
    public void generateThumbnail(
        @QueueTrigger(
            name = "job",
            queueName = "%THUMBNAIL_QUEUE_NAME%",
            connection = "AzureWebJobsStorage"
        ) String message,
        ExecutionContext context
    ) throws Exception {
        ThumbnailJob job = objectMapper.readValue(message, ThumbnailJob.class);
        job.validate();

        String connectionString = requireSetting("AzureWebJobsStorage");
        String containerName = System.getenv().getOrDefault("BLOB_CONTAINER_NAME", "artworks");
        BlobContainerClient container = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(containerName)
            .buildClient();

        BlobClient source = container.getBlobClient(job.sourceBlobName());
        ByteArrayOutputStream sourceBytes = new ByteArrayOutputStream();
        source.downloadStream(sourceBytes);

        byte[] thumbnail = thumbnailGenerator.createJpeg(sourceBytes.toByteArray(), job.maxWidth());
        BlobClient destination = container.getBlobClient(job.thumbnailBlobName());
        destination.upload(new ByteArrayInputStream(thumbnail), thumbnail.length, true);
        destination.setHttpHeaders(new BlobHttpHeaders()
            .setContentType("image/jpeg")
            .setCacheControl(CACHE_CONTROL));

        context.getLogger().info("Created thumbnail " + job.thumbnailBlobName());
    }

    private String requireSetting(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is not configured");
        }
        return value;
    }
}
