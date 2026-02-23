package com.dogu.basket.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class HazelcastConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("basket-hazelcast");
        config.setClusterName("basket-cluster");

        // basket-items cache - tek sepet öğesi (2 dk TTL)
        MapConfig basketItemsConfig = new MapConfig("basket-items");
        basketItemsConfig.setTimeToLiveSeconds(120);
        config.addMapConfig(basketItemsConfig);

        // basket-by-user cache - kullanıcının sepeti (2 dk TTL)
        MapConfig basketByUserConfig = new MapConfig("basket-by-user");
        basketByUserConfig.setTimeToLiveSeconds(120);
        config.addMapConfig(basketByUserConfig);

        // orders cache (10 dk TTL)
        MapConfig ordersConfig = new MapConfig("orders");
        ordersConfig.setTimeToLiveSeconds(600);
        config.addMapConfig(ordersConfig);

        MapConfig ordersByUserConfig = new MapConfig("orders-by-user");
        ordersByUserConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(ordersByUserConfig);

        MapConfig ordersAllConfig = new MapConfig("orders-all");
        ordersAllConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(ordersAllConfig);

        // wishlist cache (5 dk TTL)
        MapConfig wishlistConfig = new MapConfig("wishlist-by-user");
        wishlistConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(wishlistConfig);

        return config;
    }
}
