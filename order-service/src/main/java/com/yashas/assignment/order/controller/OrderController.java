package com.yashas.assignment.order.controller;

import com.yashas.assignment.order.dto.OrderCreateDto;
import com.yashas.assignment.order.dto.OrderResponseDto;
import com.yashas.assignment.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for managing order operations.
 * Provides endpoints for placing and retrieving orders.
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing customer orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /order
     * Places an order and updates inventory accordingly.
     */
    @PostMapping
    @Operation(summary = "Place a new order",
               description = "Creates a new order and updates inventory for ordered items")
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderCreateDto orderCreateDto) {
        log.info("POST request to place order for customer: {}", orderCreateDto.getCustomerId());
        OrderResponseDto order = orderService.placeOrder(orderCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /order/{orderId}
     * Retrieves order details by order ID.
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID",
               description = "Retrieves detailed information about a specific order")
    public ResponseEntity<OrderResponseDto> getOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        log.info("GET request for order: {}", orderId);
        OrderResponseDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /order/customer/{customerId}
     * Retrieves all orders for a customer.
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID",
               description = "Retrieves all orders for a specific customer")
    public ResponseEntity<List<OrderResponseDto>> getCustomerOrders(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        log.info("GET request for orders of customer: {}", customerId);
        List<OrderResponseDto> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
}
