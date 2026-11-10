package com.example.B2C.modules.order.repository;

import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryFilterTest {

    @Test
    @DisplayName("findByBuyerFiltered delegates to findAll(spec, pageable)")
    void findByBuyerFiltered_delegatesToFindAll() {
        OrderRepository repo = mock(OrderRepository.class);
        when(repo.findByBuyerFiltered(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(Order.builder().id(1L).buyerId(10L).build())));

        Page<Order> result = repo.findByBuyerFiltered(
                10L, OrderStatus.PENDING, "ORD-123", "COD",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(5000),
                PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(repo).findAll(any(Specification.class), any(Pageable.class));
    }
}
