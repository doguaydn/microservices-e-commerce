package com.dogu.invoice.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class HazelcastConfig {

    @Bean
    public Config hazelCacheConfig() {
        Config config = new Config();
        config.setInstanceName("invoice-hazelcast");
        config.setClusterName("invoice-cluster");

        // invoices cache - tek fatura sorgusu (30 dk TTL)
        MapConfig invoicesConfig = new MapConfig("invoices");
        invoicesConfig.setTimeToLiveSeconds(1800);
        config.addMapConfig(invoicesConfig);

        // invoices-by-user cache - kullanıcının faturaları (10 dk TTL)
        MapConfig invoicesByUserConfig = new MapConfig("invoices-by-user");
        invoicesByUserConfig.setTimeToLiveSeconds(600);
        config.addMapConfig(invoicesByUserConfig);

        // invoices-by-order cache - sipariş bazlı fatura (30 dk TTL)
        MapConfig invoicesByOrderConfig = new MapConfig("invoices-by-order");
        invoicesByOrderConfig.setTimeToLiveSeconds(1800);
        config.addMapConfig(invoicesByOrderConfig);

        // invoices-all cache (10 dk TTL)
        MapConfig invoicesAllConfig = new MapConfig("invoices-all");
        invoicesAllConfig.setTimeToLiveSeconds(600);
        config.addMapConfig(invoicesAllConfig);

        return config;
    }
}
