package com.example.B2C.common.storage;

import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

/**
 * Fallback {@link StoragePort} used when no real storage backend is
 * configured (e.g. local dev without a Cloudinary account, CI without
 * network access).
 * <p>
 * Registers a {@link StoragePort} bean ONLY when no other {@code StoragePort}
 * bean is on the context. Every upload attempt fails fast with HTTP 503 so
 * the FE gets a meaningful error instead of a NullPointerException.
 */
@Configuration
@ConditionalOnMissingBean(StoragePort.class)
public class NoopStorageAdapter implements StoragePort {

    private static final String MESSAGE = "Image storage is not configured for this environment. "
            + "Set app.storage.provider=cloudinary and provide CLOUDINARY_CLOUD_NAME / "
            + "CLOUDINARY_API_KEY / CLOUDINARY_API_SECRET in your .env.";

    @Override
    public StoredAsset upload(MultipartFile file, String purpose) {
        throw new StorageUnavailableException(MESSAGE);
    }

    @Override
    public void delete(String publicId) {
        // Delete is a no-op when storage is disabled — there is nothing to remove.
    }

    /**
     * Thrown by the noop adapter so {@code GlobalExceptionHandler} can
     * map it to a 503 with a useful message.
     */
    public static class StorageUnavailableException extends BusinessException {
        public StorageUnavailableException(String message) {
            super(ErrorCode.STORAGE_UNAVAILABLE, message);
        }
    }
}
