package com.yashas.assignment.inventory.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for creating allocation strategies
 */
@Component
@RequiredArgsConstructor
public class AllocationStrategyFactory {

    private final ExpiryDateStrategy expiryDateStrategy;
    private final FifoAllocationStrategy fifoStrategy;
    private final LifoAllocationStrategy lifoStrategy;

    public AllocationStrategy getStrategy(String strategyName) {
        return switch (strategyName) {
            case "EXPIRY" -> expiryDateStrategy;
            case "FIFO"   -> fifoStrategy;
            case "LIFO"   -> lifoStrategy;
            default       -> throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        };
    }
}

