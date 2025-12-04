package com.yashas.assignment.inventory;

import com.yashas.assignment.inventory.dto.BatchDto;
import com.yashas.assignment.inventory.dto.CreateProductDto;
import com.yashas.assignment.inventory.dto.ProductDto;
import com.yashas.assignment.inventory.dto.UpdateInventoryDto;
import com.yashas.assignment.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Test
    void testCreateAndRetrieveProduct() {
        // Arrange
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("CORN-001")
                .name("Corn")
                .build();

        // Act
        ProductDto createdProduct = inventoryService.createProduct(createRequest);

        // Assert
        assertNotNull(createdProduct);
        assertEquals("CORN-001", createdProduct.getProductId());
        assertEquals("Corn", createdProduct.getName());
    }

    @Test
    void testCreateProductAndAddBatch() {
        // Arrange
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("BARLEY-001")
                .name("Barley")
                .build();

        ProductDto createdProduct = inventoryService.createProduct(createRequest);

        UpdateInventoryDto batchRequest = UpdateInventoryDto.builder()
                .productId("BARLEY-001")
                .batchId("BARLEY-B001")
                .quantity(2000L)
                .expiryDate(LocalDate.now().plusMonths(12))
                .build();

        // Act
        BatchDto createdBatch = inventoryService.updateInventory(batchRequest);

        // Assert
        assertNotNull(createdBatch);
        assertEquals("BARLEY-B001", createdBatch.getBatchId());
        assertEquals("BARLEY-001", createdBatch.getProductId());
        assertEquals(2000L, createdBatch.getQuantity());
    }

    @Test
    void testGetBatchesSortedByExpiryDate() {
        // Arrange - Create product
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("OAT-001")
                .name("Oats")
                .build();

        inventoryService.createProduct(createRequest);

        // Add multiple batches with different expiry dates
        UpdateInventoryDto batch1 = UpdateInventoryDto.builder()
                .productId("OAT-001")
                .batchId("OAT-B001")
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(12))
                .build();

        UpdateInventoryDto batch2 = UpdateInventoryDto.builder()
                .productId("OAT-001")
                .batchId("OAT-B002")
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        inventoryService.updateInventory(batch1);
        inventoryService.updateInventory(batch2);

        // Act
        List<BatchDto> batches = inventoryService.getBatchesByProductId("OAT-001");

        // Assert
        assertNotNull(batches);
        assertEquals(2, batches.size());
        // Should be sorted by expiry date (earliest first)
        assertTrue(batches.get(0).getExpiryDate().isBefore(batches.get(1).getExpiryDate()));
        assertEquals("OAT-B002", batches.get(0).getBatchId()); // Expires in 6 months
        assertEquals("OAT-B001", batches.get(1).getBatchId()); // Expires in 12 months
    }

    @Test
    void testFIFOAllocationOnOrderReduction() {
        // Arrange - Create product with multiple batches
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("MILLET-001")
                .name("Millet")
                .build();

        inventoryService.createProduct(createRequest);

        // Add batch 1 (expires later)
        UpdateInventoryDto batch1 = UpdateInventoryDto.builder()
                .productId("MILLET-001")
                .batchId("MILLET-B001")
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(12))
                .build();

        // Add batch 2 (expires sooner)
        UpdateInventoryDto batch2 = UpdateInventoryDto.builder()
                .productId("MILLET-001")
                .batchId("MILLET-B002")
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        inventoryService.updateInventory(batch1);
        inventoryService.updateInventory(batch2);

        // Act - Order reduction using FIFO (should reduce from earliest expiring batch)
        UpdateInventoryDto orderReduction = UpdateInventoryDto.builder()
                .productId("MILLET-001")
                .batchId("ORDER_REDUCTION")
                .quantity(200L)
                .build();

        BatchDto result = inventoryService.updateInventory(orderReduction);

        // Assert - Should reduce from batch 2 (expires sooner)
        assertNotNull(result);
        assertEquals("MILLET-B002", result.getBatchId());
        assertEquals(300L, result.getQuantity()); // 500 - 200
    }

    @Test
    void testUpdateInventory_InsufficientQuantity() {
        // Arrange - Create product with limited batch
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("LENTIL-001")
                .name("Lentil")
                .build();

        inventoryService.createProduct(createRequest);

        UpdateInventoryDto batch = UpdateInventoryDto.builder()
                .productId("LENTIL-001")
                .batchId("LENTIL-B001")
                .quantity(100L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        inventoryService.updateInventory(batch);

        // Act & Assert
        UpdateInventoryDto largeReduction = UpdateInventoryDto.builder()
                .productId("LENTIL-001")
                .batchId("LENTIL-B001")
                .quantity(200L)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateInventory(largeReduction);
        });
    }

    @Test
    void testCreateProduct_Duplicate() {
        // Arrange
        CreateProductDto createRequest = CreateProductDto.builder()
                .productId("SOY-001")
                .name("Soy")
                .build();

        inventoryService.createProduct(createRequest);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.createProduct(createRequest);
        });
    }

    @Test
    void testGetBatches_ProductNotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.getBatchesByProductId("NONEXISTENT-001");
        });
    }
}

