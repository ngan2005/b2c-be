package com.example.B2C.modules.catalog.repository;

import com.example.B2C.modules.catalog.entity.VariantOptionValue;
import com.example.B2C.modules.catalog.entity.VariantOptionValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, VariantOptionValueId> {

    @Modifying
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.id.variantId = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);

    @Modifying
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.id.variantId IN :variantIds")
    void deleteByVariantIds(@Param("variantIds") List<Long> variantIds);

    @Query("SELECT vov FROM VariantOptionValue vov WHERE vov.id.variantId = :variantId")
    List<VariantOptionValue> findByVariantId(@Param("variantId") Long variantId);

    @Query("SELECT vov FROM VariantOptionValue vov WHERE vov.id.variantId IN :variantIds")
    List<VariantOptionValue> findByVariantIds(@Param("variantIds") List<Long> variantIds);
}

