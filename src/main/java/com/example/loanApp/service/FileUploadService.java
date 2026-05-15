package com.example.loanApp.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Service
public class FileUploadService {
    private final String BUCKET_NAME = "simuloans-uploads";
    private final String REGION = "hel1";
    private final String ACCESS_KEY = "VK3XS17DOHVGTMILL8OB";
    private final String SECRET_KEY = "v0KUeDQ2NCfEJs9T7bCDknWqWHKWMJqeCpQJZipY";

    // Initialize the S3 Client for Hetzner
    private final S3Client s3Client = S3Client.builder()
            .endpointOverride(URI.create("https://" + REGION + ".your-objectstorage.com"))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
            ))
            .region(Region.of(REGION))
            .build();

    /**
     * Uploads a file to Hetzner Object Storage
     */
    public String uploadToHetzner(String applicationId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file object provided");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String key = String.format("applications/%s/documents/%d-%s",
                    applicationId, System.currentTimeMillis(), originalFilename);

            // Build the request with Public Read ACL and Metadata
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .metadata(Map.of("application-id", applicationId))
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.%s.your-objectstorage.com/%s", BUCKET_NAME, REGION, key);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading document to Hetzner: " + e.getMessage(), e);
        }
    }
}
