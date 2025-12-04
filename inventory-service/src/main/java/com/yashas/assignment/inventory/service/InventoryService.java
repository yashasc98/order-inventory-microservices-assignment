package com.yashas.assignment.inventory.service;

import com.yashas.assignment.inventory.dto.CreateProductDto;
import com.yashas.assignment.inventory.dto.UpdateInventoryDto;
import com.yashas.assignment.inventory.dto.BatchDto;
import com.yashas.assignment.inventory.dto.ProductDto;
import com.yashas.assignment.inventory.entity.Batch;
import com.yashas.assignment.inventory.entity.Product;
import com.yashas.assignment.inventory.factory.AllocationStrategyFactory;
import com.yashas.assignment.inventory.repository.BatchRepository;
import com.yashas.assignment.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final AllocationStrategyFactory allocationStrategyFactory;

    /**
     * POST /inventory/product - Add a new product
     */
    @Transactional
    public ProductDto createProduct(CreateProductDto request) {
        log.info("Creating new product: {}", request.getProductId());

        // Check if product already exists
        if (productRepository.findByProductId(request.getProductId()).isPresent()) {
            throw new IllegalArgumentException("Product already exists: " + request.getProductId());
        }

        Product product = Product.builder()
                .productId(request.getProductId())
                .name(request.getName())
                .build();
        Product saved = productRepository.save(product);
        log.info("Product created successfully: {}", saved.getProductId());
        return convertToProductDto(saved);
    }

    /**
     * POST /inventory/update - Update inventory
     * Can be used for:
     * 1. Adding new batch to existing product
     * 2. Reducing batch quantity when order is placed
     */
    @Transactional
    public BatchDto updateInventory(UpdateInventoryDto request) {
        log.info("Updating inventory - Product: {}, Batch: {}, Quantity: {}",
                request.getProductId(), request.getBatchId(), request.getQuantity());

        // Product must exist
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.getProductId()));

        // Special case: if batchId is ORDER_REDUCTION, find the first available batch
        Batch targetBatch;
        List<Batch> availableBatches = List.of();
        if ("ORDER_REDUCTION".equals(request.getBatchId())) {
            // For orders, reduce from the first available batch (FIFO by expiry date)
            availableBatches = batchRepository.findByProduct(product);
            if (availableBatches.isEmpty()) {
                throw new IllegalArgumentException("No batches available for product: " + request.getProductId());
            }
            availableBatches = allocationStrategyFactory.getStrategy("EXPIRY")
                    .allocate(availableBatches, request.getQuantity());
            targetBatch = availableBatches.get(0);
        } else {
            // Specific batch ID provided
            targetBatch = batchRepository.findByBatchId(request.getBatchId()).orElse(null);
        }

        if (targetBatch != null) {
            // Batch exists - reduce quantity (order placed scenario)
            long totalAvailable = availableBatches.stream().mapToLong(Batch::getQuantity).sum();
            long qtyToDeduct = request.getQuantity();
            if (qtyToDeduct > totalAvailable) {
                throw new IllegalArgumentException("Insufficient total quantity. Available: "
                        + totalAvailable + ", Requested: " + qtyToDeduct);
            }

            List<BatchDto> affected = new ArrayList<>();

            for (Batch batch : availableBatches) {
                if (qtyToDeduct == 0) break;

                long available = batch.getQuantity();

                if (available >= qtyToDeduct) {
                    // Consume partially and done
                    batch.setQuantity(available - qtyToDeduct);
                    batchRepository.save(batch);
                    affected.add(convertToBatchDto(batch));
                    qtyToDeduct = 0;
                } else {
                    // Consume entire batch and continue
                    batch.setQuantity(0L);
                    batchRepository.save(batch);
                    affected.add(convertToBatchDto(batch));
                    qtyToDeduct -= available;
                }
            }

            log.info("Order reduction completed. Batches affected: {}", affected.size());

            return affected.get(0);
        } else {
            // New batch - add it
            if (request.getExpiryDate() == null) {
                throw new IllegalArgumentException("Expiry date is required for new batch");
            }
            Batch newBatch = Batch.builder()
                    .batchId(request.getBatchId())
                    .product(product)
                    .quantity(request.getQuantity())
                    .expiryDate(request.getExpiryDate())
                    .build();
            Batch saved = batchRepository.save(newBatch);
            log.info("New batch {} created for product {}", request.getBatchId(), request.getProductId());
            return convertToBatchDto(saved);
        }
    }


    /**
     * GET /inventory/{productId} - Returns list of inventory batches sorted by expiry date
     */
    public List<BatchDto> getBatchesByProductId(String productId) {
        log.info("Fetching batches for product: {}", productId);

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        List<Batch> batches = batchRepository.findByProductOrderByExpiryDateAsc(product);
        return batches.stream()
                .map(this::convertToBatchDto)
                .collect(Collectors.toList());
    }


    /**
     * Convert Product entity to DTO
     */
    private ProductDto convertToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .name(product.getName())
                .build();
    }

    /**
     * Convert Batch entity to DTO
     */
    private BatchDto convertToBatchDto(Batch batch) {
        return BatchDto.builder()
                .id(batch.getId())
                .batchId(batch.getBatchId())
                .productId(batch.getProduct().getProductId())
                .quantity(batch.getQuantity())
                .expiryDate(batch.getExpiryDate())
                .build();
    }
}


