package com.yashas.assignment.inventory.factory;

import com.yashas.assignment.inventory.entity.Batch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FIFO (First In First Out) allocation strategy - allocates from batches with the earliest expiry date
 */
@Component
public class FifoAllocationStrategy implements AllocationStrategy {

    @Override
    public List<Batch> allocate(List<Batch> availableBatches, Long quantityNeeded) {
        List<Batch> allocatedBatches = new ArrayList<>();
        long remaining = quantityNeeded;

        // Batches are already sorted by expiry date (ascending), so we allocate from the earliest expiry
        for (Batch batch : availableBatches) {
            if (remaining <= 0) break;

            Long available = batch.getQuantity();
            long toAllocate = Math.min(available, remaining);

            Batch allocationCopy = new Batch();
            allocationCopy.setId(batch.getId());
            allocationCopy.setBatchId(batch.getBatchId());
            allocationCopy.setQuantity(toAllocate);
            allocationCopy.setProduct(batch.getProduct());
            allocatedBatches.add(allocationCopy);

            remaining -= toAllocate;
        }

        if (remaining > 0) {
            throw new IllegalArgumentException("Insufficient inventory available");
        }

        return allocatedBatches;
    }
}

