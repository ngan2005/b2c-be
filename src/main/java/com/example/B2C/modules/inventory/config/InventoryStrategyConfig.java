package com.example.B2C.modules.inventory.config;

import com.example.B2C.modules.inventory.strategy.E0NoLockStrategy;
import com.example.B2C.modules.inventory.strategy.E1OptimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E2PessimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E3OptimisticRetryStrategy;
import com.example.B2C.modules.inventory.strategy.InventoryStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * Selects the active {@link InventoryStrategy} based on the
 * {@code inventory.locking.strategy} property. The default profile uses E1
 * (optimistic locking) which is the production recommendation.
 */
@Configuration
public class InventoryStrategyConfig {

    @Value("${inventory.locking.strategy:E1}")
    private String activeStrategy;

    @Bean
    @Primary
    public InventoryStrategy activeInventoryStrategy(
            E0NoLockStrategy e0,
            E1OptimisticStrategy e1,
            E2PessimisticStrategy e2,
            E3OptimisticRetryStrategy e3) {
        Map<String, InventoryStrategy> map = new HashMap<>();
        map.put("E0", e0);
        map.put("E1", e1);
        map.put("E2", e2);
        map.put("E3", e3);
        InventoryStrategy strategy = map.get(activeStrategy.toUpperCase());
        if (strategy == null) {
            throw new IllegalStateException("Unknown inventory locking strategy: " + activeStrategy
                    + ". Expected one of E0, E1, E2, E3.");
        }
        return strategy;
    }
}
