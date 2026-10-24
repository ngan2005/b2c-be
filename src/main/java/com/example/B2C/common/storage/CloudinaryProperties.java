package com.example.B2C.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the storage backend.
 *
 * <p>Bound to the {@code app.storage.cloudinary.*} namespace. Reading is
 * optional — the bean is only registered when {@code app.storage.provider=cloudinary}.
 *
 * <p>Credentials are sourced from environment variables / .env (NOT hard-coded):
 * <ul>
 *   <li>{@code CLOUDINARY_CLOUD_NAME}</li>
 *   <li>{@code CLOUDINARY_API_KEY}</li>
 *   <li>{@code CLOUDINARY_API_SECRET}</li>
 * </ul>
 */
@Data
@ConfigurationProperties(prefix = "app.storage.cloudinary")
public class CloudinaryProperties {

    /** Cloudinary cloud name (e.g. {@code my-shop-prod}). */
    private String cloudName;

    /** Cloudinary API key. */
    private String apiKey;

    /** Cloudinary API secret. */
    private String apiSecret;

    /**
     * Top-level folder under which every uploaded asset is stored.
     * Defaults to {@code b2c} so dev / staging / prod can share one
     * Cloudinary account without colliding.
     */
    private String folder = "b2c";

    /**
     * Maximum file size in bytes accepted by the BE upload endpoint.
     * Default 5 MB. Larger files are rejected with HTTP 413.
     */
    private long maxFileSizeBytes = 5L * 1024 * 1024;
}
