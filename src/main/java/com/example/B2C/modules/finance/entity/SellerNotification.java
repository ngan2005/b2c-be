package com.example.B2C.modules.finance.entity;

import com.example.B2C.common.entity.BaseEntity;
import com.example.B2C.modules.seller.entity.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seller_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_name", length = 255)
    private String ruleName;

    @Column(name = "rule_percent", precision = 5, scale = 2)
    private BigDecimal rulePercent;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
}
