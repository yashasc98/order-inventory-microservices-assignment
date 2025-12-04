package com.yashas.assignment.inventory.config;

import com.yashas.assignment.inventory.entity.Batch;
import com.yashas.assignment.inventory.entity.Product;
import com.yashas.assignment.inventory.repository.BatchRepository;
import com.yashas.assignment.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initializes sample data on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing sample inventory data...");

        // Create Product 1: Wheat
        Product wheat = Product.builder()
                .productId("WHEAT-001")
                .name("Wheat")
                .build();
        wheat = productRepository.save(wheat);
        log.info("Created product: {}", wheat.getProductId());

        // Add batches for Wheat
        Batch batch1 = Batch.builder()
                .batchId("WHEAT-B001")
                .product(wheat)
                .quantity(1000L)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();
        batchRepository.save(batch1);
        log.info("Added batch: {}", batch1.getBatchId());

        Batch batch2 = Batch.builder()
                .batchId("WHEAT-B002")
                .product(wheat)
                .quantity(500L)
                .expiryDate(LocalDate.now().plusMonths(9))
                .build();
        batchRepository.save(batch2);
        log.info("Added batch: {}", batch2.getBatchId());

        // Create Product 2: Rice
        Product rice = Product.builder()
                .productId("RICE-001")
                .name("Rice")
                .build();
        rice = productRepository.save(rice);
        log.info("Created product: {}", rice.getProductId());

        // Add batches for Rice
        Batch batch3 = Batch.builder()
                .batchId("RICE-B001")
                .product(rice)
                .quantity(2000L)
                .expiryDate(LocalDate.now().plusMonths(12))
                .build();
        batchRepository.save(batch3);
        log.info("Added batch: {}", batch3.getBatchId());

        Batch batch4 = Batch.builder()
                .batchId("RICE-B002")
                .product(rice)
                .quantity(1500L)
                .expiryDate(LocalDate.now().plusMonths(8))
                .build();
        batchRepository.save(batch4);
        log.info("Added batch: {}", batch4.getBatchId());

        // Create Product 3: Sugar
        Product sugar = Product.builder()
                .productId("SUGAR-001")
                .name("Sugar")
                .build();
        sugar = productRepository.save(sugar);
        log.info("Created product: {}", sugar.getProductId());

        // Add batch for Sugar
        Batch batch5 = Batch.builder()
                .batchId("SUGAR-B001")
                .product(sugar)
                .quantity(3000L)
                .expiryDate(LocalDate.now().plusMonths(18))
                .build();
        batchRepository.save(batch5);
        log.info("Added batch: {}", batch5.getBatchId());

        log.info("Sample inventory data initialization completed!");
    }
}

