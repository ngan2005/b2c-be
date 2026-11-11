package com.example.B2C.modules.upload.controller;

import com.example.B2C.common.exception.GlobalExceptionHandler;
import com.example.B2C.common.storage.StoragePort;
import com.example.B2C.common.storage.StoredAsset;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UploadController uploadController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(uploadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/upload - upload image successfully returns Cloudinary URL and metadata")
    void uploadImage_success_returnsUrlAndMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-bytes".getBytes()
        );

        StoredAsset asset = StoredAsset.builder()
                .publicId("b2c/uploads/test-uuid")
                .secureUrl("https://res.cloudinary.com/test/image/upload/v12345/test.png")
                .resourceType("image")
                .format("png")
                .originalFilename("test-avatar.png")
                .bytes(16)
                .build();

        when(storagePort.upload(any(), eq("avatars"))).thenReturn(asset);

        mockMvc.perform(multipart("/api/v1/upload")
                        .file(file)
                        .param("purpose", "avatars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/test/image/upload/v12345/test.png"))
                .andExpect(jsonPath("$.data.secureUrl").value("https://res.cloudinary.com/test/image/upload/v12345/test.png"))
                .andExpect(jsonPath("$.data.publicId").value("b2c/uploads/test-uuid"))
                .andExpect(jsonPath("$.data.format").value("png"))
                .andExpect(jsonPath("$.data.originalFilename").value("test-avatar.png"))
                .andExpect(jsonPath("$.data.bytes").value(16));
    }

    @Test
    @DisplayName("POST /api/v1/upload - empty file returns 400 Bad Request")
    void uploadImage_emptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Uploaded file cannot be empty"));
    }
}
