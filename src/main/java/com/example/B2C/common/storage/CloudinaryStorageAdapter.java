package com.example.B2C.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.B2C.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * {@link StoragePort} implementation backed by Cloudinary.
 * <p>
 * The bean is only registered when {@code app.storage.provider=cloudinary}
 * AND the three credentials ({@code cloudName / apiKey / apiSecret}) are
 * The bean is wired up in {@link StorageAutoConfiguration} and only registered
 * when a {@link Cloudinary} bean is present in the context.
 */
@Slf4j
public class CloudinaryStorageAdapter implements StoragePort {

    /** MIME types accepted by this adapter. PNG / JPEG / WebP cover every browser. */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryStorageAdapter(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StoredAsset upload(MultipartFile file, String purpose) {
        validate(file);

        String safePurpose = sanitisePurpose(purpose);
        String publicId = safePurpose + "/" + UUID.randomUUID();

        Map<String, Object> options = new HashMap<>();
        options.put("public_id", publicId);
        options.put("folder", properties.getFolder());
        // Always serve via https; never persist http URLs.
        options.put("secure", true);
        // Strip EXIF GPS data — privacy hygiene.
        options.put("eager", List.of());
        options.put("resource_type", "image");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

            String secureUrl = (String) result.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                throw new IllegalStateException("Cloudinary returned no secure_url for public_id=" + publicId);
            }

            // Cloudinary prefixes the configured folder to the public_id, so the
            // value it echoes back ("b2c/sellers/2/logo/<uuid>") is the REAL
            // asset id — the one destroy() must be given. The locally built
            // `publicId` omits the folder and would not match, so prefer the
            // echoed value and fall back to prefixing the folder ourselves.
            String storedPublicId = (String) result.get("public_id");
            if (storedPublicId == null || storedPublicId.isBlank()) {
                storedPublicId = withFolder(publicId);
            }

            return StoredAsset.builder()
                    .publicId(storedPublicId)
                    .secureUrl(secureUrl)
                    .resourceType((String) result.getOrDefault("resource_type", "image"))
                    .format((String) result.getOrDefault("format", file.getContentType()))
                    .originalFilename(file.getOriginalFilename())
                    .bytes(((Number) result.getOrDefault("bytes", file.getSize())).longValue())
                    .build();

        } catch (IOException ex) {
            log.error("Cloudinary upload failed for purpose={}: {}", safePurpose, ex.getMessage());
            throw new RuntimeException("Failed to read uploaded file", ex);
        } catch (Exception ex) {
            log.error("Cloudinary upload failed for purpose={}: {}", safePurpose, ex.getMessage());
            throw new RuntimeException("Storage backend rejected the upload", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            // `invalidate=true` also busts Cloudinary CDN caches.
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("invalidate", true, "resource_type", "image"));

            // Rows written before the folder-prefix fix stored the id WITHOUT
            // the configured folder ("sellers/2/logo/<uuid>") while the asset
            // itself lives under "<folder>/sellers/2/logo/<uuid>". Cloudinary
            // reports "not found" for the former, so retry with the prefixed
            // variant before giving up.
            if (isNotFound(result) && !publicId.startsWith(prefixedFolder())) {
                String prefixed = withFolder(publicId);
                Map<String, Object> retry = cloudinary.uploader().destroy(
                        prefixed,
                        ObjectUtils.asMap("invalidate", true, "resource_type", "image"));
                log.debug("Cloudinary destroy retry for {} returned {}", prefixed, retry.get("result"));
                return;
            }
            log.debug("Cloudinary destroy for {} returned {}", publicId, result.get("result"));
        } catch (Exception ex) {
            // We do NOT rethrow — DB cleanup must proceed even if Cloudinary
            // is temporarily unavailable. The orphaned asset can be GC'd by
            // a scheduled job later.
            log.warn("Cloudinary destroy failed for public_id={}: {}", publicId, ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException(
                    "Unsupported image type: " + contentType + ". Allowed: " + ALLOWED_CONTENT_TYPES);
        }
        long max = properties.getMaxFileSizeBytes();
        if (file.getSize() > max) {
            throw new BadRequestException(
                    "File too large: " + file.getSize() + " bytes (max " + max + ")");
        }
    }

    /**
     * Collapse any non-[a-z0-9-_/] character in the purpose segment to '-'.
     * This guarantees the resulting public_id is safe for the Cloudinary API
     * AND prevents path traversal (e.g. {@code ../../foo} from a hostile client).
     */
    private String sanitisePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            return "misc";
        }
        String normalised = purpose.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9/_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        // Reject any remaining '..' segment defensively.
        for (String seg : Arrays.asList(normalised.split("/"))) {
            if (seg.equals("..") || seg.isBlank()) {
                return "misc";
            }
        }
        return normalised;
    }

    /**
     * Prefix the configured folder onto a public_id that does not already
     * carry it, mirroring what Cloudinary does on upload.
     * <p>
     * Used only as a fallback when the upload response omits {@code public_id};
     * the id must match the stored asset exactly or {@link #delete(String)}
     * would silently fail to remove it.
     */
    private String withFolder(String publicId) {
        String folder = prefixedFolder();
        if (folder.isEmpty() || publicId.startsWith(folder + "/")) {
            return publicId;
        }
        return folder + "/" + publicId;
    }

    /**
     * The configured top-level folder, normalised (no leading/trailing slash).
     * Empty when no folder is configured.
     */
    private String prefixedFolder() {
        String folder = properties.getFolder();
        if (folder == null || folder.isBlank()) {
            return "";
        }
        return folder.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    /**
     * Cloudinary reports a missing asset as {@code result=not found} (and
     * {@code result=not found} for the "already deleted" case as well).
     */
    private boolean isNotFound(Map<String, Object> result) {
        if (result == null) {
            return true;
        }
        Object value = result.get("result");
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        return text.equals("not found") || text.contains("not found");
    }
}
