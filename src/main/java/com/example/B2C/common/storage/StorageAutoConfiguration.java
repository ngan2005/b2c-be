package com.example.B2C.common.storage;

import com.cloudinary.Cloudinary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Selects the {@link StoragePort} implementation at startup:
 * <ul>
 *   <li>If a {@link Cloudinary} bean exists (i.e. {@code app.storage.provider=cloudinary}
 *       and credentials are configured — see
 *       {@link CloudinaryStorageAutoConfiguration}), use
 *       {@link CloudinaryStorageAdapter}.</li>
 *   <li>Otherwise, fall back to {@link NoopStorageAdapter} so the application
 *       can still boot (uploads will fail fast with HTTP 503).</li>
 * </ul>
 * Without this class, only one of the two adapters would be a {@code @Component}
 * and Spring's auto-config conditions would be evaluated in the wrong order,
 * leaving the context with no {@code StoragePort} bean at all.
 */
@Configuration
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnBean(Cloudinary.class)
    public CloudinaryStorageAdapter cloudinaryStorageAdapter(Cloudinary cloudinary,
                                                              CloudinaryProperties properties) {
        return new CloudinaryStorageAdapter(cloudinary, properties);
    }

    @Bean
    @ConditionalOnMissingBean(StoragePort.class)
    public NoopStorageAdapter noopStorageAdapter() {
        return new NoopStorageAdapter();
    }
}
