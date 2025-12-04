package com.yashas.assignment.inventory.factory;

import com.yashas.assignment.inventory.entity.Batch;

import java.util.List;

public interface AllocationStrategy {

    List<Batch> allocate(List<Batch> availableBatches, Long quantityNeeded);

}

