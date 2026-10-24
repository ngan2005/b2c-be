package com.example.B2C.common.storage;

import com.cloudinary.Cloudinary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for the storage layer.
 * <p>
 * Active only when {@code app.storage.provider=cloudinary}. The provider
 * key lets us introduce additional backends (Supabase, S3, …) without
 * deleting this class.
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryStorageAutoConfiguration {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        if (isBlank(properties.getCloudName())
                || isBlank(properties.getApiKey())
                || isBlank(properties.getApiSecret())) {
            throw new IllegalStateException(
                    "Cloudinary is enabled (app.storage.provider=cloudinary) but credentials are missing. "
                            + "Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET "
                            + "in your environment or .env file.");
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", properties.getCloudName());
        config.put("api_key", properties.getApiKey());
        config.put("api_secret", properties.getApiSecret());
        // Always return https URLs.
        config.put("secure", "true");

        return new Cloudinary(config);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
