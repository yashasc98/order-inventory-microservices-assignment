package com.yashas.assignment.inventory.factory;

import com.yashas.assignment.inventory.entity.Batch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LIFO (Last In First Out) allocation strategy - allocates from batches with latest expiry date
 */
@Component
public class LifoAllocationStrategy implements AllocationStrategy {

    @Override
    public List<Batch> allocate(List<Batch> availableBatches, Long quantityNeeded) {
        List<Batch> allocatedBatches = new ArrayList<>();
        long remaining = quantityNeeded;

        // Reverse the list to allocate from latest expiry date first
        List<Batch> reversed = new ArrayList<>(availableBatches);
        Collections.reverse(reversed);

        for (Batch batch : reversed) {
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

