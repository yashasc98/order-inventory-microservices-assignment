package com.yashas.assignment.order.dto;

import com.yashas.assignment.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for order response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;

    private String orderId;

    private String customerId;

    private OrderStatus status;

    private List<OrderItemDto> orderItems;

    private Double totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

