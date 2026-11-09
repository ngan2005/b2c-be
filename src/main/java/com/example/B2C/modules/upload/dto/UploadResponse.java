package com.example.B2C.modules.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upload file/image response")
public class UploadResponse {

    @Schema(description = "Direct HTTPS URL to access the uploaded file", example = "https://res.cloudinary.com/demo/image/upload/v123456/sample.jpg")
    private String url;

    @Schema(description = "Secure HTTPS URL to access the uploaded file", example = "https://res.cloudinary.com/demo/image/upload/v123456/sample.jpg")
    private String secureUrl;

    @Schema(description = "Public ID of the uploaded asset in Cloudinary", example = "b2c/uploads/uuid-1234")
    private String publicId;

    @Schema(description = "Resource type (e.g. image)", example = "image")
    private String resourceType;

    @Schema(description = "File format or MIME type", example = "image/jpeg")
    private String format;

    @Schema(description = "Original filename", example = "my_photo.png")
    private String originalFilename;

    @Schema(description = "File size in bytes", example = "1048576")
    private long bytes;
}
