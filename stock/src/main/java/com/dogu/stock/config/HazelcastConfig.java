package com.dogu.stock.config;

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
        config.setInstanceName("stock-hazelcast");
        config.setClusterName("stock-cluster");

        // products cache - tek ürün sorguları (10 dk TTL)
        MapConfig productsConfig = new MapConfig("products");
        productsConfig.setTimeToLiveSeconds(600);
        config.addMapConfig(productsConfig);

        // products-all cache - tüm ürün listesi (5 dk TTL)
        MapConfig productsAllConfig = new MapConfig("products-all");
        productsAllConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(productsAllConfig);

        return config;
    }
}
