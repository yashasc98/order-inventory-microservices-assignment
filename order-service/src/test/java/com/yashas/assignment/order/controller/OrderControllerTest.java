package com.yashas.assignment.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yashas.assignment.order.dto.OrderCreateDto;
import com.yashas.assignment.order.dto.OrderItemCreateDto;
import com.yashas.assignment.order.dto.OrderResponseDto;
import com.yashas.assignment.order.entity.OrderStatus;
import com.yashas.assignment.order.exception.GlobalExceptionHandler;
import com.yashas.assignment.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;
    private OrderResponseDto orderResponseDto;
    private OrderCreateDto orderCreateDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        List<OrderItemCreateDto> items = new ArrayList<>();
        items.add(OrderItemCreateDto.builder()
                .productId("WHEAT-001")
                .quantity(100L)
                .build());

        orderCreateDto = OrderCreateDto.builder()
                .customerId("CUST-001")
                .items(items)
                .build();

        orderResponseDto = OrderResponseDto.builder()
                .id(1L)
                .orderId("ORD-12345")
                .customerId("CUST-001")
                .status(OrderStatus.CONFIRMED)
                .orderItems(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testPlaceOrder_Success() throws Exception {
        // Arrange
        when(orderService.placeOrder(any(OrderCreateDto.class))).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", equalTo("ORD-12345")))
                .andExpect(jsonPath("$.customerId", equalTo("CUST-001")))
                .andExpect(jsonPath("$.status", equalTo("CONFIRMED")));

        verify(orderService, times(1)).placeOrder(any(OrderCreateDto.class));
    }

    @Test
    void testPlaceOrder_NoItems() throws Exception {
        // Arrange
        OrderCreateDto invalidOrder = OrderCreateDto.builder()
                .customerId("CUST-001")
                .items(new ArrayList<>())
                .build();

        // Act & Assert - validation should fail and controller should not call the service
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderCreateDto.class));
    }

    @Test
    void testPlaceOrder_ProductNotFound() throws Exception {
        // Arrange
        when(orderService.placeOrder(any(OrderCreateDto.class)))
                .thenThrow(new IllegalArgumentException("Product not found or no inventory"));

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).placeOrder(any(OrderCreateDto.class));
    }

    @Test
    void testPlaceOrder_MultipleItems() throws Exception {
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

        when(orderService.placeOrder(any(OrderCreateDto.class))).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(multiItemOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", equalTo("CONFIRMED")));

        verify(orderService, times(1)).placeOrder(any(OrderCreateDto.class));
    }

    @Test
    void testGetOrderById_Success() throws Exception {
        // Arrange
        when(orderService.getOrderById("ORD-12345")).thenReturn(orderResponseDto);

        // Act & Assert
        mockMvc.perform(get("/api/order/ORD-12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", equalTo("ORD-12345")))
                .andExpect(jsonPath("$.customerId", equalTo("CUST-001")));

        verify(orderService, times(1)).getOrderById("ORD-12345");
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById("NONEXISTENT"))
                .thenThrow(new IllegalArgumentException("Order not found: NONEXISTENT"));

        // Act & Assert
        mockMvc.perform(get("/api/order/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).getOrderById("NONEXISTENT");
    }

    @Test
    void testGetOrdersByCustomerId_Success() throws Exception {
        // Arrange
        List<OrderResponseDto> orders = Arrays.asList(orderResponseDto);
        when(orderService.getOrdersByCustomerId("CUST-001")).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/order/customer/CUST-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId", equalTo("CUST-001")));

        verify(orderService, times(1)).getOrdersByCustomerId("CUST-001");
    }

    @Test
    void testGetOrdersByCustomerId_Empty() throws Exception {
        // Arrange
        when(orderService.getOrdersByCustomerId("UNKNOWN")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/order/customer/UNKNOWN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getOrdersByCustomerId("UNKNOWN");
    }

    @Test
    void testPlaceOrder_InvalidRequest_MissingCustomerId() throws Exception {
        // Arrange
        String invalidJson = "{\"items\": [{\"productId\": \"WHEAT-001\", \"quantity\": 100}]}";

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderCreateDto.class));
    }
}
