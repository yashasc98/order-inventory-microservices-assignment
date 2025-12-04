package com.yashas.assignment.inventory.service;

import com.yashas.assignment.inventory.dto.BatchDto;
import com.yashas.assignment.inventory.dto.CreateProductDto;
import com.yashas.assignment.inventory.dto.ProductDto;
import com.yashas.assignment.inventory.dto.UpdateInventoryDto;
import com.yashas.assignment.inventory.entity.Batch;
import com.yashas.assignment.inventory.entity.Product;
import com.yashas.assignment.inventory.factory.AllocationStrategyFactory;
import com.yashas.assignment.inventory.factory.ExpiryDateStrategy;
import com.yashas.assignment.inventory.repository.BatchRepository;
import com.yashas.assignment.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private AllocationStrategyFactory allocationStrategyFactory;

    @Mock
    private ExpiryDateStrategy expiryDateStrategy;

    @InjectMocks
    private InventoryService inventoryService;

    private Product testProduct;
    private Batch testBatch;
    private CreateProductDto createProductDto;
    private UpdateInventoryDto updateInventoryDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .productId("WHEAT-001")
                .name("Wheat")
                .createdAt(LocalDateTime.now())
                .build();

        testBatch = Batch.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .product(testProduct)
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
    void testCreateProduct_Success() {
        // Arrange
        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductDto result = inventoryService.createProduct(createProductDto);

        // Assert
        assertNotNull(result);
        assertEquals("WHEAT-001", result.getProductId());
        assertEquals("Wheat", result.getName());
        verify(productRepository, times(1)).findByProductId("WHEAT-001");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_ProductAlreadyExists() {
        // Arrange
        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.createProduct(createProductDto);
        });
        verify(productRepository, times(1)).findByProductId("WHEAT-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateInventory_AddNewBatch_Success() {
        // Arrange
        Batch newBatch = Batch.builder()
                .id(2L)
                .batchId("WHEAT-B001")
                .product(testProduct)
                .quantity(100L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.of(testProduct));
        when(batchRepository.findByBatchId("WHEAT-B001")).thenReturn(Optional.empty());
        when(batchRepository.save(any(Batch.class))).thenReturn(newBatch);

        // Act
        BatchDto result = inventoryService.updateInventory(updateInventoryDto);

        // Assert
        assertNotNull(result);
        assertEquals("WHEAT-B001", result.getBatchId());
        assertEquals("WHEAT-001", result.getProductId());
        assertEquals(100L, result.getQuantity());
        verify(batchRepository, times(1)).save(any(Batch.class));
    }

    @Test
    void testUpdateInventory_InsufficientQuantity() {
        // Arrange
        Batch existingBatch = Batch.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .product(testProduct)
                .quantity(50L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        updateInventoryDto.setBatchId("WHEAT-B001");
        updateInventoryDto.setQuantity(100L);

        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.of(testProduct));
        when(batchRepository.findByBatchId("WHEAT-B001")).thenReturn(Optional.of(existingBatch));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateInventory(updateInventoryDto);
        });
        verify(batchRepository, never()).save(any(Batch.class));
    }

    @Test
    void testUpdateInventory_ProductNotFound() {
        // Arrange
        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateInventory(updateInventoryDto);
        });
    }

    @Test
    void testGetBatchesByProductId_Success() {
        // Arrange
        Batch batch2 = Batch.builder()
                .id(2L)
                .batchId("WHEAT-B002")
                .product(testProduct)
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(9))
                .build();

        List<Batch> batches = Arrays.asList(testBatch, batch2);

        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.of(testProduct));
        when(batchRepository.findByProductOrderByExpiryDateAsc(testProduct)).thenReturn(batches);

        // Act
        List<BatchDto> result = inventoryService.getBatchesByProductId("WHEAT-001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("WHEAT-B001", result.get(0).getBatchId());
        assertEquals("WHEAT-B002", result.get(1).getBatchId());
        verify(batchRepository, times(1)).findByProductOrderByExpiryDateAsc(testProduct);
    }

    @Test
    void testGetBatchesByProductId_ProductNotFound() {
        // Arrange
        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.getBatchesByProductId("WHEAT-001");
        });
    }

    @Test
    void testUpdateInventory_OrderReductionFIFO() {
        // Arrange
        Batch fifoBatch = Batch.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .product(testProduct)
                .quantity(1000L)
                .expiryDate(LocalDate.now().plusMonths(3))
                .build();

        Batch reducedBatch = Batch.builder()
                .id(1L)
                .batchId("WHEAT-B001")
                .product(testProduct)
                .quantity(900L)
                .expiryDate(LocalDate.now().plusMonths(3))
                .build();

        UpdateInventoryDto orderReductionDto = UpdateInventoryDto.builder()
                .productId("WHEAT-001")
                .batchId("ORDER_REDUCTION")
                .quantity(100L)
                .build();

        when(productRepository.findByProductId("WHEAT-001")).thenReturn(Optional.of(testProduct));
        when(batchRepository.findByProduct(testProduct)).thenReturn(Arrays.asList(fifoBatch));
        when(batchRepository.save(any(Batch.class))).thenReturn(reducedBatch);
        when(allocationStrategyFactory.getStrategy(anyString())).thenReturn(expiryDateStrategy);
        when(expiryDateStrategy.allocate(anyList(), anyLong())).thenReturn(Arrays.asList(fifoBatch));

        // Act
        BatchDto result = inventoryService.updateInventory(orderReductionDto);

        // Assert
        assertNotNull(result);
        assertEquals(900L, result.getQuantity());
        verify(batchRepository, times(1)).findByProduct(testProduct);
    }
}

