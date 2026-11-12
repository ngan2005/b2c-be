package com.example.B2C.modules.inventory.dto;

import com.example.B2C.common.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerInventoryOverviewDto {
    private long totalSkus;
    private long lowStockCount;
    private long outOfStockCount;
    private PageResponse<SellerInventoryItemDto> items;
}

