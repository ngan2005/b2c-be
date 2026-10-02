package com.example.B2C.modules.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VariantOptionValueId implements Serializable {

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "option_value_id")
    private Long optionValueId;
}
