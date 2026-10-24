package com.example.B2C.modules.catalog.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryUpdateRequest {

    @Size(max = 150, message = "Name must be at most 150 characters")
    private String name;

    @Size(max = 200, message = "Slug must be at most 200 characters")
    private String slug;

    private Long parentId;

    private String iconUrl;

    private Boolean isActive;
}
