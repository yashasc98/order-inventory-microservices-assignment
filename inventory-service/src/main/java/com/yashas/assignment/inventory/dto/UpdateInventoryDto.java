package com.yashas.assignment.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for POST /inventory/update
 * Used for:
 * 1. Adding new batch to existing product
 * 2. Reducing batch quantity after order placement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotNull(message = "Quantity is required")
    private Long quantity;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;  // Required for adding new batch, optional for quantity reduction
}

