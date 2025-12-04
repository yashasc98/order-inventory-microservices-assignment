package com.yashas.assignment.order.service;

import com.yashas.assignment.order.client.InventoryServiceClient;
import com.yashas.assignment.order.dto.OrderCreateDto;
import com.yashas.assignment.order.dto.OrderItemCreateDto;
import com.yashas.assignment.order.dto.OrderItemDto;
import com.yashas.assignment.order.dto.OrderResponseDto;
import com.yashas.assignment.order.entity.Order;
import com.yashas.assignment.order.entity.OrderItem;
import com.yashas.assignment.order.entity.OrderStatus;
import com.yashas.assignment.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing order operations.
 * Implements the business logic for creating and processing orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    /**
     * Place a new order and update inventory
     */
    public OrderResponseDto placeOrder(OrderCreateDto orderCreateDto) {
        log.info("Processing new order for customer: {}", orderCreateDto.getCustomerId());

        if (orderCreateDto.getItems() == null || orderCreateDto.getItems().isEmpty()) {
            log.error("Order must contain at least one item");
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        String orderId = generateOrderId();

        try {
            // Verify inventory availability for all items
            for (OrderItemCreateDto item : orderCreateDto.getItems()) {
                if (!inventoryServiceClient.checkInventoryAvailability(item.getProductId())) {
                    log.error("Product not found or no inventory: {}", item.getProductId());
                    throw new IllegalArgumentException("Product not found or no inventory: " + item.getProductId());
                }
            }

            // Create order
            Order order = Order.builder()
                    .orderId(orderId)
                    .customerId(orderCreateDto.getCustomerId())
                    .status(OrderStatus.PENDING)
                    .orderItems(new ArrayList<>())
                    .build();

            Order savedOrder = orderRepository.save(order);
            log.info("Order created with ID: {}", orderId);

            // Create and save order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemCreateDto itemDto : orderCreateDto.getItems()) {
                OrderItem orderItem = OrderItem.builder()
                        .order(savedOrder)
                        .productId(itemDto.getProductId())
                        .quantity(itemDto.getQuantity())
                        .build();
                orderItems.add(orderItem);
            }
            savedOrder.setOrderItems(orderItems);

            // Update inventory for each item
            boolean inventoryUpdatedSuccessfully = true;
            for (OrderItem item : orderItems) {
                try {
                    inventoryServiceClient.updateInventory(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Failed to update inventory for item: {}", item.getProductId(), e);
                    inventoryUpdatedSuccessfully = false;
                    break;
                }
            }

            // Update order status based on inventory update result
            if (inventoryUpdatedSuccessfully) {
                savedOrder.setStatus(OrderStatus.CONFIRMED);
                log.info("Order confirmed: {}", orderId);
            } else {
                savedOrder.setStatus(OrderStatus.FAILED);
                log.error("Order failed: {}", orderId);
            }

            Order finalOrder = orderRepository.save(savedOrder);
            return mapToOrderResponseDto(finalOrder);

        } catch (IllegalArgumentException e) {
            log.error("Invalid order request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing order: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve order by order ID
     */
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(String orderId) {
        log.info("Fetching order: {}", orderId);

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found: " + orderId);
                });

        return mapToOrderResponseDto(order);
    }

    /**
     * Retrieve all orders by customer ID
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByCustomerId(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::mapToOrderResponseDto)
                .toList();
    }

    // Helper methods
    private OrderResponseDto mapToOrderResponseDto(Order order) {
        List<OrderItemDto> itemDTOs = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                itemDTOs.add(OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build());
            }
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .orderItems(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

