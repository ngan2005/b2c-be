package com.example.B2C.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variant_option_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantOptionValue {

    @EmbeddedId
    private VariantOptionValueId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("variantId")
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("optionValueId")
    @JoinColumn(name = "option_value_id", nullable = false)
    private ProductOptionValue optionValue;
}
