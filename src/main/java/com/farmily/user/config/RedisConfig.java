package com.farmily.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

// 設定 Redis 連線池 JedisPool (Thread-safe)
@Configuration
public class RedisConfig {

    @Value("${app.redis.host}")
    private String host;

    @Value("${app.redis.port}")
    private int port;


    // 提供一個 JedisPool，讓 TokenService 需要時跟它借連線
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // 關掉連線池自動註冊 JMX 管理 bean
        // 因為 IntelliJ 啟動時開了 Spring 的 JMX(-Dspring.jmx.enabled=true)，
        // 兩邊會搶用同一個名字註冊而衝突，這裡關掉就不會撞
        poolConfig.setJmxEnabled(false);

        return new JedisPool(poolConfig, host, port);
    }
}