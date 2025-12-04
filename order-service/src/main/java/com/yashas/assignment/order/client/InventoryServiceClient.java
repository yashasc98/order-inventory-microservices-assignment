package com.yashas.assignment.order.client;

import com.yashas.assignment.order.dto.InventoryUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for communicating with the Inventory Service.
 * Handles inter-service communication via REST API.
 */
@Component
@Slf4j
public class InventoryServiceClient {

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryServiceClient(RestTemplate restTemplate,
                                  @Value("${inventory.service.url:http://localhost:8081}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Call the Inventory Service to update inventory after order placement
     * Reduces the quantity from the first available batch
     */
    public void updateInventory(String productId, Long quantityToReduce) {
        log.info("Calling Inventory Service to reduce inventory for product: {} by quantity: {}",
                productId, quantityToReduce);

        try {
            String url = inventoryServiceUrl + "/inventory/update";

            // For order-based inventory reduction, we'll use a placeholder batch ID
            // The inventory service will reduce from the first available batch
            InventoryUpdateDto updateDto = InventoryUpdateDto.builder()
                    .productId(productId)
                    .batchId("ORDER_REDUCTION")  // Placeholder - actual batch will be determined by inventory service
                    .quantity(quantityToReduce)
                    .build();

            restTemplate.postForObject(url, updateDto, String.class);
            log.info("Inventory updated successfully for product: {}", productId);
        } catch (RestClientException e) {
            log.error("Failed to update inventory for product: {}", productId, e);
            throw new RuntimeException("Failed to communicate with Inventory Service: " + e.getMessage(), e);
        }
    }

    /**
     * Check if product exists and has sufficient inventory
     */
    public boolean checkInventoryAvailability(String productId) {
        log.info("Checking inventory availability for product: {}", productId);

        try {
            String url = inventoryServiceUrl + "/inventory/" + productId;
            restTemplate.getForObject(url, Object.class);
            log.info("Product: {} has available inventory", productId);
            return true;
        } catch (RestClientException e) {
            log.warn("Failed to check inventory for product: {} - {}", productId, e.getMessage());
            return false;
        }
    }
}

