package com.example.B2C.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option", uniqueConstraints = {
    @UniqueConstraint(name = "uq_product_option", columnNames = {"product_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
