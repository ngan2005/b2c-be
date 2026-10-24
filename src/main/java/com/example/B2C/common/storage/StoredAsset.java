package com.example.B2C.common.storage;

import lombok.Builder;
import lombok.Data;

/**
 * Immutable result of a successful upload to the storage backend.
 * Wraps the bits of metadata the application needs to persist alongside
 * the URL (so that the asset can later be deleted from the backend).
 */
@Data
@Builder
public class StoredAsset {

    /**
     * The asset's public id inside the storage backend (e.g. the
     * Cloudinary {@code public_id}). Persist this so the asset can be
     * removed later.
     */
    private String publicId;

    /**
     * Secure HTTPS URL that the browser can load directly. The {@code https://}
     * scheme is enforced; do not persist unsigned {@code http://} URLs.
     */
    private String secureUrl;

    /**
     * Resource type returned by the backend — typically {@code image},
     * {@code video} or {@code raw}. Used for diagnostics.
     */
    private String resourceType;

    /**
     * MIME type detected by the backend (e.g. {@code image/jpeg}).
     */
    private String format;

    /**
     * Original filename supplied by the client. Stored for audit only.
     */
    private String originalFilename;

    /**
     * File size in bytes as reported by the backend.
     */
    private long bytes;
}
