package com.yashas.assignment.inventory.repository;

import com.yashas.assignment.inventory.entity.Batch;
import com.yashas.assignment.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    
    Optional<Batch> findByBatchId(String batchId);

    List<Batch> findByProduct(Product product);
    
    List<Batch> findByProductOrderByExpiryDateAsc(Product product);

}

