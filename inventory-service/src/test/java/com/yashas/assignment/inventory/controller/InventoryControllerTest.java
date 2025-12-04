package com.yashas.assignment.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yashas.assignment.inventory.dto.BatchDto;
import com.yashas.assignment.inventory.dto.CreateProductDto;
import com.yashas.assignment.inventory.dto.ProductDto;
import com.yashas.assignment.inventory.dto.UpdateInventoryDto;
import com.yashas.assignment.inventory.exception.GlobalExceptionHandler;
import com.yashas.assignment.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private ObjectMapper objectMapper;
    private ProductDto productDto;
    private BatchDto batchDto;
    private CreateProductDto createProductDto;
    private UpdateInventoryDto updateInventoryDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        productDto = ProductDto.builder()
                .id(1L)
                .productId("WHEAT-001")
                .name("Wheat")
                .build();

        batchDto = BatchDto.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .productId("WHEAT-001")
                .quantity(1000L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        createProductDto = CreateProductDto.builder()
                .productId("WHEAT-001")
                .name("Wheat")
                .build();

        updateInventoryDto = UpdateInventoryDto.builder()
                .productId("WHEAT-001")
                .batchId("WHEAT-B001")
                .quantity(100L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();
    }

    @Test
    void testGetBatches_Success() throws Exception {
        // Arrange
        List<BatchDto> batches = Arrays.asList(batchDto);
        when(inventoryService.getBatchesByProductId("WHEAT-001")).thenReturn(batches);

        // Act & Assert
        mockMvc.perform(get("/inventory/WHEAT-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].batchId", equalTo("WHEAT-B001")))
                .andExpect(jsonPath("$[0].productId", equalTo("WHEAT-001")))
                .andExpect(jsonPath("$[0].quantity", equalTo(1000)));

        verify(inventoryService, times(1)).getBatchesByProductId("WHEAT-001");
    }

    @Test
    void testGetBatches_ProductNotFound() throws Exception {
        // Arrange
        when(inventoryService.getBatchesByProductId("NONEXISTENT")).thenThrow(
                new IllegalArgumentException("Product not found: NONEXISTENT"));

        // Act & Assert
        mockMvc.perform(get("/inventory/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 instead of 400

        verify(inventoryService, times(1)).getBatchesByProductId("NONEXISTENT");
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        // Arrange
        when(inventoryService.createProduct(any(CreateProductDto.class))).thenReturn(productDto);

        // Act & Assert
        mockMvc.perform(post("/inventory/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId", equalTo("WHEAT-001")))
                .andExpect(jsonPath("$.name", equalTo("Wheat")));

        verify(inventoryService, times(1)).createProduct(any(CreateProductDto.class));
    }

    @Test
    void testCreateProduct_InvalidRequest() throws Exception {
        // Arrange
        CreateProductDto invalidDto = CreateProductDto.builder()
                .productId("")
                .name("Wheat")
                .build();

        // Act & Assert
        mockMvc.perform(post("/inventory/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).createProduct(any(CreateProductDto.class));
    }

    @Test
    void testCreateProduct_DuplicateProduct() throws Exception {
        // Arrange
        when(inventoryService.createProduct(any(CreateProductDto.class)))
                .thenThrow(new IllegalArgumentException("Product already exists: WHEAT-001"));

        // Act & Assert
        mockMvc.perform(post("/inventory/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, times(1)).createProduct(any(CreateProductDto.class));
    }

    @Test
    void testUpdateInventory_AddBatch_Success() throws Exception {
        // Arrange
        when(inventoryService.updateInventory(any(UpdateInventoryDto.class))).thenReturn(batchDto);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInventoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batchId", equalTo("WHEAT-B001")))
                .andExpect(jsonPath("$.productId", equalTo("WHEAT-001")))
                .andExpect(jsonPath("$.quantity", equalTo(1000)));

        verify(inventoryService, times(1)).updateInventory(any(UpdateInventoryDto.class));
    }

    @Test
    void testUpdateInventory_ReduceQuantity_Success() throws Exception {
        // Arrange
        UpdateInventoryDto reduceDto = UpdateInventoryDto.builder()
                .productId("WHEAT-001")
                .batchId("WHEAT-B001")
                .quantity(100L)
                .build();

        BatchDto reducedBatch = BatchDto.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .productId("WHEAT-001")
                .quantity(900L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        when(inventoryService.updateInventory(any(UpdateInventoryDto.class))).thenReturn(reducedBatch);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reduceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", equalTo(900)));

        verify(inventoryService, times(1)).updateInventory(any(UpdateInventoryDto.class));
    }

    @Test
    void testUpdateInventory_InsufficientQuantity() throws Exception {
        // Arrange
        UpdateInventoryDto reduceDto = UpdateInventoryDto.builder()
                .productId("WHEAT-001")
                .batchId("WHEAT-B001")
                .quantity(2000L)
                .build();

        when(inventoryService.updateInventory(any(UpdateInventoryDto.class)))
                .thenThrow(new IllegalArgumentException("Insufficient quantity"));

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reduceDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, times(1)).updateInventory(any(UpdateInventoryDto.class));
    }

    @Test
    void testUpdateInventory_ProductNotFound() throws Exception {
        // Arrange
        UpdateInventoryDto invalidDto = UpdateInventoryDto.builder()
                .productId("NONEXISTENT")
                .batchId("BATCH-001")
                .quantity(100L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        when(inventoryService.updateInventory(any(UpdateInventoryDto.class)))
                .thenThrow(new IllegalArgumentException("Product not found: NONEXISTENT"));

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, times(1)).updateInventory(any(UpdateInventoryDto.class));
    }
}

