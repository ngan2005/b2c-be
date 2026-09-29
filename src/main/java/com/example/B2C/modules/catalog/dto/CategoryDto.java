package com.example.B2C.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer level;
    private String path;
    private Long parentId;
    private List<CategoryDto> children;
}
