package com.example.B2C.modules.catalog.repository;

import com.example.B2C.modules.catalog.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Query("""
        SELECT opt FROM ProductOption opt
        WHERE opt.product.id = :productId
        ORDER BY opt.sortOrder ASC, opt.id ASC
        """)
    List<ProductOption> findByProductIdOrderBySortOrder(@Param("productId") Long productId);
}
