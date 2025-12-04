package com.yashas.assignment.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for POST /inventory/product
 * Used to add new products to the inventory
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String name;
}

