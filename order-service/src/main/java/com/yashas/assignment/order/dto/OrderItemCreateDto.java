package com.yashas.assignment.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating an order item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCreateDto {

    private String productId;

    private Long quantity;
}

