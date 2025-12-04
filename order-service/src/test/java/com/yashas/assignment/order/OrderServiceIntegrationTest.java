package com.yashas.assignment.order;

import com.yashas.assignment.order.client.InventoryServiceClient;
import com.yashas.assignment.order.dto.OrderCreateDto;
import com.yashas.assignment.order.dto.OrderItemCreateDto;
import com.yashas.assignment.order.dto.OrderResponseDto;
import com.yashas.assignment.order.entity.OrderStatus;
import com.yashas.assignment.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private InventoryServiceClient inventoryServiceClient;

    private OrderCreateDto orderCreateDto;

    @BeforeEach
    void setUp() {
        List<OrderItemCreateDto> items = new ArrayList<>();
        items.add(OrderItemCreateDto.builder()
                .productId("WHEAT-001")
                .quantity(50L)
                .build());

        orderCreateDto = OrderCreateDto.builder()
                .customerId("CUST-001")
                .items(items)
                .build();

        // Default stubbing for integration tests: inventory checks succeed and updates do nothing
        when(inventoryServiceClient.checkInventoryAvailability(anyString())).thenReturn(true);
        doNothing().when(inventoryServiceClient).updateInventory(anyString(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void testPlaceOrder_Success() {
        // Act
        OrderResponseDto result = orderService.placeOrder(orderCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals("CUST-001", result.getCustomerId());
        assertNotNull(result.getOrderId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(1, result.getOrderItems().size());
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
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange - First place an order
        OrderResponseDto createdOrder = orderService.placeOrder(orderCreateDto);
        String orderId = createdOrder.getOrderId();

        // Act
        OrderResponseDto result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals("CUST-001", result.getCustomerId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void testGetOrdersByCustomerId_Success() {
        // Arrange - Place an order
        orderService.placeOrder(orderCreateDto);

        // Act
        List<OrderResponseDto> results = orderService.getOrdersByCustomerId("CUST-001");

        // Assert
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.stream().allMatch(o -> "CUST-001".equals(o.getCustomerId())));
    }

    @Test
    void testGetOrdersByCustomerId_Empty() {
        // Act
        List<OrderResponseDto> results = orderService.getOrdersByCustomerId("UNKNOWN-CUSTOMER");

        // Assert
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void testGetOrderById_NotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById("NONEXISTENT-ORD");
        });
    }

    @Test
    void testPlaceOrder_MultipleItems() {
        // Arrange
        List<OrderItemCreateDto> items = new ArrayList<>();
        items.add(OrderItemCreateDto.builder()
                .productId("WHEAT-001")
                .quantity(50L)
                .build());
        items.add(OrderItemCreateDto.builder()
                .productId("RICE-001")
                .quantity(30L)
                .build());

        OrderCreateDto multiItemOrder = OrderCreateDto.builder()
                .customerId("CUST-002")
                .items(items)
                .build();

        // Act
        OrderResponseDto result = orderService.placeOrder(multiItemOrder);

        // Assert
        assertNotNull(result);
        assertEquals("CUST-002", result.getCustomerId());
        assertEquals(2, result.getOrderItems().size());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }
}
