package com.example.B2C.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDto {

    @NotBlank
    private String reason;  // DAMAGED, WRONG_ITEM, NOT_AS_DESCRIBED, CHANGED_MIND, OTHER

    private String description;  // optional free text

    private List<Long> orderItemIds;  // if null, all items returned
}
