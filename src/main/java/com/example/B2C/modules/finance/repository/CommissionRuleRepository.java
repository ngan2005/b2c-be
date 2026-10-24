package com.example.B2C.modules.finance.repository;

import com.example.B2C.modules.finance.entity.CommissionRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    Optional<CommissionRule> findFirstByCategoryIdAndIsActiveTrueOrderByEffectiveFromDesc(Long categoryId);

    Optional<CommissionRule> findFirstByCategoryIdIsNullAndIsActiveTrueOrderByEffectiveFromDesc();

    Page<CommissionRule> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT r FROM CommissionRule r ORDER BY r.createdAt DESC")
    Page<CommissionRule> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM CommissionRecord r WHERE r.rule.id = :ruleId
        """)
    boolean existsByRuleId(@Param("ruleId") Long ruleId);
}
