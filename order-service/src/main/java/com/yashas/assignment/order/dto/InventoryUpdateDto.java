package com.yashas.assignment.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inventory update (internal use for client communication)
 * Used to communicate with Inventory Service /inventory/update endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateDto {

    private String productId;

    private String batchId;

    private Long quantity;
}

