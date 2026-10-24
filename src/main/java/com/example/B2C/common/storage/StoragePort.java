package com.example.B2C.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Cross-module port for binary asset storage (product images, etc.).
 * <p>
 * Implementations live in {@code common.storage} and currently wrap Cloudinary.
 * Cart / Order / Catalog modules depend on this interface only, so the
 * backing service can be swapped (S3, Supabase Storage, …) without touching
 * business code.
 */
public interface StoragePort {

    /**
     * Upload a file and return the stored asset metadata.
     *
     * @param file    the multipart payload from the request
     * @param purpose short semantic key used as the asset's public id prefix
     *                (e.g. {@code "products/{productId}"} or {@code "avatars"}).
     *                The implementation is responsible for sanitising it.
     * @return the stored asset, never {@code null}
     * @throws com.example.B2C.common.exception.BadRequestException  if the
     *         file is empty, the wrong MIME type, or exceeds the configured size
     * @throws RuntimeException                                      on backend failure
     */
    StoredAsset upload(MultipartFile file, String purpose);

    /**
     * Delete a previously uploaded asset by its public id.
     * <p>
     * Implementations MUST be idempotent — deleting a missing asset is not
     * an error and MUST NOT throw.
     *
     * @param publicId the public id returned by {@link #upload(MultipartFile, String)}
     */
    void delete(String publicId);
}
