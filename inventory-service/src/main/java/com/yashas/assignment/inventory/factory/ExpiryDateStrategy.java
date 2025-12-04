package com.yashas.assignment.inventory.factory;

import com.yashas.assignment.inventory.entity.Batch;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Expiry Date allocation strategy - allocates from batches based on expiry date
 */
@Component
public class ExpiryDateStrategy implements AllocationStrategy {

    @Override
    public List<Batch> allocate(List<Batch> availableBatches, Long quantityNeeded) {
        return availableBatches.stream()
                .sorted(Comparator.comparing(Batch::getExpiryDate))
                .toList();
    }

}
