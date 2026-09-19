package com.example.B2C.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option_value", uniqueConstraints = {
    @UniqueConstraint(name = "uq_option_value", columnNames = {"option_id", "value"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption option;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
