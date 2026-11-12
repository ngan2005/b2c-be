package com.example.B2C.modules.upload.controller;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.storage.StoragePort;
import com.example.B2C.common.storage.StoredAsset;
import com.example.B2C.modules.upload.dto.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Upload", description = "File and image upload endpoints via Cloudinary")
public class UploadController {

    private final StoragePort storagePort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload image to Cloudinary",
            description = "Accepts a single multipart image file (image/jpeg, image/png, image/webp) up to 5MB, "
                    + "uploads it to Cloudinary storage, and returns the public URL and metadata."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Empty file, unsupported MIME type, or invalid purpose parameter"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "Storage backend not configured"
            )
    })
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @Parameter(description = "Image file (JPEG/PNG/WebP, max 5MB)")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional semantic purpose / folder prefix (e.g. 'avatars', 'products', 'uploads')")
            @RequestParam(value = "purpose", required = false, defaultValue = "uploads") String purpose
    ) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        String safePurpose = (purpose == null || purpose.isBlank()) ? "uploads" : purpose.trim();
        StoredAsset asset = storagePort.upload(file, safePurpose);

        UploadResponse response = UploadResponse.builder()
                .url(asset.getSecureUrl())
                .secureUrl(asset.getSecureUrl())
                .publicId(asset.getPublicId())
                .resourceType(asset.getResourceType())
                .format(asset.getFormat())
                .originalFilename(asset.getOriginalFilename())
                .bytes(asset.getBytes())
                .build();

        log.info("Uploaded file '{}' via /api/v1/upload (publicId={}, url={})",
                asset.getOriginalFilename(), asset.getPublicId(), asset.getSecureUrl());

        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }
}
