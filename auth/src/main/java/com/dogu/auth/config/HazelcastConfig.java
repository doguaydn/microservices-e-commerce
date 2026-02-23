package com.dogu.auth.config;

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
        config.setInstanceName("auth-hazelcast");
        config.setClusterName("auth-cluster");

        // users cache - tek kullanıcı sorguları (5 dk TTL)
        MapConfig usersConfig = new MapConfig("users");
        usersConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(usersConfig);

        // users-all cache - tüm kullanıcı listesi (5 dk TTL)
        MapConfig usersAllConfig = new MapConfig("users-all");
        usersAllConfig.setTimeToLiveSeconds(300);
        config.addMapConfig(usersAllConfig);

        return config;
    }
}
