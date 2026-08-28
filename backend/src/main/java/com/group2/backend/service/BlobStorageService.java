package com.group2.backend.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.PublicAccessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
        @Value("${app.blob.connection-string}") String connectionString,
        @Value("${app.blob.container-name}") String containerName
    ) {
        this.containerClient = new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(containerName)
            .buildClient();

        this.containerClient.createIfNotExists();
        // Allow anonymous read access to individual blobs so browsers can load image URLs directly
        try {
            this.containerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        } catch (Exception ex) {
            log.warn("Could not set container access policy to public: {}", ex.getMessage());
        }
    }

    public void upload(String blobName, byte[] content, String contentType) {
        var blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(new ByteArrayInputStream(content), content.length, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
    }

    public void deleteIfExists(String blobName) {
        if (blobName == null || blobName.isBlank()) {
            return;
        }
        try {
            containerClient.getBlobClient(blobName).deleteIfExists();
        } catch (Exception ex) {
            log.warn("Failed to delete blob {}", blobName, ex);
        }
    }

    public String toBlobUrl(String blobName) {
        if (blobName == null || blobName.isBlank()) {
            return null;
        }
        return containerClient.getBlobClient(blobName).getBlobUrl();
    }

    public void deleteAll() {
        try {
            containerClient.listBlobs()
                .forEach(item -> deleteIfExists(item.getName()));
            log.info("Cleared all blobs from container");
        } catch (Exception ex) {
            log.warn("Failed to clear all blobs from container: {}", ex.getMessage());
        }
    }
}
