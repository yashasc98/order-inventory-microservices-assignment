package com.yashas.assignment.inventory.controller;

import com.yashas.assignment.inventory.dto.CreateProductDto;
import com.yashas.assignment.inventory.dto.UpdateInventoryDto;
import com.yashas.assignment.inventory.dto.BatchDto;
import com.yashas.assignment.inventory.dto.ProductDto;
import com.yashas.assignment.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * GET /inventory/{productId} - Returns list of inventory batches sorted by expiry date
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Get batches for a product",
            description = "Returns all batches for a product sorted by expiry date")
    public ResponseEntity<List<BatchDto>> getBatches(@PathVariable String productId) {
        log.info("GET /inventory/{} - Fetching batches", productId);
        try{
            List<BatchDto> batches = inventoryService.getBatchesByProductId(productId);
            return ResponseEntity.ok(batches);
        } catch (IllegalArgumentException e) {
            log.error("Product not found: {}", productId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /inventory/product - Add a new product
     */
    @PostMapping("/product")
    @Operation(summary = "Create a new product",
            description = "Creates a new product with name, description, and price")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductDto request) {
        log.info("POST /inventory/product - Creating product: {}", request.getProductId());
        ProductDto product = inventoryService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * POST /inventory/update - Update inventory
     * Can be used for:
     * 1. Adding new batch to existing product
     * 2. Reducing batch quantity when order is placed
     */
    @PostMapping("/update")
    @Operation(summary = "Update inventory batch",
            description = "Add new batch to product or reduce batch quantity when order is placed")
    public ResponseEntity<BatchDto> updateInventory(@Valid @RequestBody UpdateInventoryDto request) {
        log.info("POST /inventory/update - Product: {}, Batch: {}, Quantity: {}",
                request.getProductId(), request.getBatchId(), request.getQuantity());
        BatchDto batch = inventoryService.updateInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }
}

