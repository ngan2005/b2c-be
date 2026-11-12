package com.example.B2C.modules.inventory.dto;

import com.example.B2C.modules.inventory.entity.LoadTestResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadTestExecutionResponse {
    private LoadTestResult result;
    private List<Integer> successfulRequestIds;
    private List<AttemptDetailDto> attempts;
}

