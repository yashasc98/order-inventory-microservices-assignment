package com.yashas.assignment.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for creating a new order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDto {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemCreateDto> items;
}
