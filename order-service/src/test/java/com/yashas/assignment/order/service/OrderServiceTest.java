package com.yashas.assignment.order.service;

import com.yashas.assignment.order.client.InventoryServiceClient;
import com.yashas.assignment.order.dto.OrderCreateDto;
import com.yashas.assignment.order.dto.OrderItemCreateDto;
import com.yashas.assignment.order.dto.OrderResponseDto;
import com.yashas.assignment.order.entity.Order;
import com.yashas.assignment.order.entity.OrderStatus;
import com.yashas.assignment.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateDto orderCreateDto;
    private Order order;

    @BeforeEach
    void setUp() {
        List<OrderItemCreateDto> items = new ArrayList<>();
        items.add(OrderItemCreateDto.builder()
                .productId("WHEAT-001")
                .quantity(100L)
                .build());

        orderCreateDto = OrderCreateDto.builder()
                .customerId("CUST-001")
                .items(items)
                .build();

        order = Order.builder()
                .id(1L)
                .orderId("ORD-12345")
                .customerId("CUST-001")
                .status(OrderStatus.CONFIRMED)
                .orderItems(new ArrayList<>())
                .build();
    }

    @Test
    void testPlaceOrder_Success() {
        // Arrange
        when(inventoryServiceClient.checkInventoryAvailability("WHEAT-001")).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponseDto result = orderService.placeOrder(orderCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals("CUST-001", result.getCustomerId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(inventoryServiceClient, times(1)).checkInventoryAvailability("WHEAT-001");
        verify(inventoryServiceClient, times(1)).updateInventory(anyString(), anyLong());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_NoItems() {
        // Arrange
        OrderCreateDto invalidOrder = OrderCreateDto.builder()
                .customerId("CUST-001")
                .items(new ArrayList<>())
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(invalidOrder);
        });
        verify(inventoryServiceClient, never()).checkInventoryAvailability(anyString());
    }

    @Test
    void testPlaceOrder_ProductNotFound() {
        // Arrange
        when(inventoryServiceClient.checkInventoryAvailability("WHEAT-001")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(orderCreateDto);
        });
        verify(inventoryServiceClient, times(1)).checkInventoryAvailability("WHEAT-001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testPlaceOrder_InventoryUpdateFails() {
        // Arrange
        when(inventoryServiceClient.checkInventoryAvailability("WHEAT-001")).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        doThrow(new RuntimeException("Inventory service error"))
                .when(inventoryServiceClient).updateInventory(anyString(), anyLong());

        // Act
        OrderResponseDto result = orderService.placeOrder(orderCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.FAILED, result.getStatus());
        verify(inventoryServiceClient, times(1)).checkInventoryAvailability("WHEAT-001");
        verify(inventoryServiceClient, times(1)).updateInventory(anyString(), anyLong());
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findByOrderId("ORD-12345")).thenReturn(Optional.of(order));

        // Act
        OrderResponseDto result = orderService.getOrderById("ORD-12345");

        // Assert
        assertNotNull(result);
        assertEquals("ORD-12345", result.getOrderId());
        assertEquals("CUST-001", result.getCustomerId());
        verify(orderRepository, times(1)).findByOrderId("ORD-12345");
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findByOrderId("NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById("NONEXISTENT");
        });
    }

    @Test
    void testGetOrdersByCustomerId_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByCustomerId("CUST-001")).thenReturn(orders);

        // Act
        List<OrderResponseDto> result = orderService.getOrdersByCustomerId("CUST-001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CUST-001", result.get(0).getCustomerId());
        verify(orderRepository, times(1)).findByCustomerId("CUST-001");
    }

    @Test
    void testGetOrdersByCustomerId_Empty() {
        // Arrange
        when(orderRepository.findByCustomerId("UNKNOWN")).thenReturn(new ArrayList<>());

        // Act
        List<OrderResponseDto> result = orderService.getOrdersByCustomerId("UNKNOWN");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testPlaceOrder_MultipleItems() {
        // Arrange
        List<OrderItemCreateDto> items = new ArrayList<>();
        items.add(OrderItemCreateDto.builder()
                .productId("WHEAT-001")
                .quantity(100L)
                .build());
        items.add(OrderItemCreateDto.builder()
                .productId("RICE-001")
                .quantity(50L)
                .build());

        OrderCreateDto multiItemOrder = OrderCreateDto.builder()
                .customerId("CUST-002")
                .items(items)
                .build();

        when(inventoryServiceClient.checkInventoryAvailability("WHEAT-001")).thenReturn(true);
        when(inventoryServiceClient.checkInventoryAvailability("RICE-001")).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponseDto result = orderService.placeOrder(multiItemOrder);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(inventoryServiceClient, times(2)).checkInventoryAvailability(anyString());
        verify(inventoryServiceClient, times(2)).updateInventory(anyString(), anyLong());
    }
}
