package com.java.sportshub.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.java.sportshub.exceptions.StorageException;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class CloudflareR2Service {

  private static final String PRODUCTS_FOLDER = "products";
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName;
  private final String publicBaseUrl;
  private final Duration defaultUploadExpiration;
  private final Region region = Region.US_EAST_1;

  public CloudflareR2Service(
      @Value("${cloudflare.r2.accountId}") String accountId,
      @Value("${cloudflare.r2.accessKey}") String accessKey,
      @Value("${cloudflare.r2.secretKey}") String secretKey,
      @Value("${cloudflare.r2.bucket}") String bucketName,
      @Value("${cloudflare.r2.publicBaseUrl:}") String publicBaseUrl,
      @Value("${cloudflare.r2.defaultUploadExpirationSeconds:900}") long defaultUploadExpirationSeconds) {

    validateRequired("cloudflare.r2.accountId", accountId);
    validateRequired("cloudflare.r2.accessKey", accessKey);
    validateRequired("cloudflare.r2.secretKey", secretKey);
    validateRequired("cloudflare.r2.bucket", bucketName);

    if (defaultUploadExpirationSeconds <= 0) {
      throw new IllegalStateException("cloudflare.r2.defaultUploadExpirationSeconds must be greater than zero");
    }

    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
    S3Configuration configuration = S3Configuration.builder()
        .pathStyleAccessEnabled(true)
        .build();

    this.s3Client = S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .serviceConfiguration(configuration)
        .region(region)
        .build();

    this.s3Presigner = S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .serviceConfiguration(configuration)
        .region(region)
        .build();

    this.bucketName = bucketName;
    this.publicBaseUrl = normalizePublicBaseUrl(publicBaseUrl);
    this.defaultUploadExpiration = Duration.ofSeconds(defaultUploadExpirationSeconds);
  }

  public String uploadProductImage(Long productId, MultipartFile file) {
    if (productId == null) {
      throw new StorageException("The product identifier is required to upload an image.");
    }

    if (file == null || file.isEmpty()) {
      throw new StorageException("A valid file is required to upload the product image.");
    }

    String objectKey = buildProductImageKey(productId, file.getOriginalFilename());

    try (InputStream inputStream = file.getInputStream()) {
      putObject(objectKey, inputStream, file.getSize(), file.getContentType());
    } catch (IOException exception) {
      throw new StorageException("Failed to read the content of the file to upload.", exception);
    }

    return objectKey;
  }

  public void deleteObject(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      throw new StorageException("The object identifier to delete cannot be empty.");
    }

    try {
      DeleteObjectRequest request = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey)
          .build();

      s3Client.deleteObject(request);
    } catch (S3Exception exception) {
      throw new StorageException("Failed to delete the object '" + objectKey + "' in Cloudflare R2.",
          exception);
    }
  }

  public URL generateUploadUrl(String objectKey, Duration expiration) {
    Duration effectiveExpiration = expiration != null ? expiration : defaultUploadExpiration;

    try {
      PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
          .signatureDuration(effectiveExpiration)
          .putObjectRequest(builder -> builder
              .bucket(bucketName)
              .key(objectKey)
              .build())
          .build();

      PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
      return presignedRequest.url();
    } catch (S3Exception exception) {
      throw new StorageException(
          "Failed to generate the presigned upload URL for the object '" + objectKey + "'.",
          exception);
    }
  }

  public URL generateDownloadUrl(String objectKey, Duration expiration) {
    Duration effectiveExpiration = expiration != null ? expiration : defaultUploadExpiration;

    try {
      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(effectiveExpiration)
          .getObjectRequest(builder -> builder
              .bucket(bucketName)
              .key(objectKey)
              .build())
          .build();

      PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
      return presignedRequest.url();
    } catch (S3Exception exception) {
      throw new StorageException(
          "Failed to generate the presigned download URL for the object '" + objectKey + "'.",
          exception);
    }
  }

  public String resolvePublicUrl(String objectKey) {
    if (publicBaseUrl == null || objectKey == null || objectKey.isBlank()) {
      return null;
    }
    return publicBaseUrl + objectKey;
  }

  private void putObject(String objectKey, InputStream inputStream, long contentLength, String contentType) {
    if (contentLength <= 0) {
      throw new StorageException("The file to upload must have content.");
    }

    try {
      PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey);

      if (contentType != null && !contentType.isBlank()) {
        requestBuilder = requestBuilder.contentType(contentType);
      }

      PutObjectRequest request = requestBuilder.build();

      s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
    } catch (S3Exception exception) {
      throw new StorageException("Failed to upload the object '" + objectKey + "' to Cloudflare R2.", exception);
    }
  }

  private static void validateRequired(String property, String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(property + " is required.");
    }
  }

  private static String normalizePublicBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return null;
    }

    return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
  }

  private String buildProductImageKey(Long productId, String originalFilename) {
    String sanitized = sanitizeFilename(originalFilename);
    String name = extractName(originalFilename);
    String extension = extractExtension(originalFilename);
    return PRODUCTS_FOLDER + "/" + productId + "/" + UUID.randomUUID() + extension;
  }

  private String sanitizeFilename(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "";
    }

    String cleanFilename = StringUtils.getFilename(originalFilename);

    String normalized = cleanFilename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    int extensionIndex = normalized.lastIndexOf('.');

    if (extensionIndex == -1) {
      return "-" + normalized;
    }

    String name = normalized.substring(0, extensionIndex);
    String extension = normalized.substring(extensionIndex).toLowerCase(Locale.ROOT);

    if (name.isBlank()) {
      name = "image";
    }

    return "-" + name + extension;
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "";
    }

    String cleanFilename = StringUtils.getFilename(originalFilename);
    return cleanFilename.substring(cleanFilename.lastIndexOf('.'));
  }

  private String extractName(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "";
    }

    String cleanFilename = StringUtils.getFilename(originalFilename);
    return cleanFilename.substring(0, cleanFilename.lastIndexOf('.'));
  }
}
