package com.yashas.assignment.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDto {
    private Long id;
    private String batchId;
    private String productId;
    private Long quantity;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}

