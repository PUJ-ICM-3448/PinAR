package com.pinar.comunitiesservice;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisCloudConnectionIT {

    @Test
    @EnabledIfEnvironmentVariable(named = "REDIS_PASSWORD", matches = ".+")
    void lettuceConnectsToRedisCloud() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String username = System.getenv().getOrDefault("REDIS_USERNAME", "default");
        String password = System.getenv("REDIS_PASSWORD");
        boolean ssl = Boolean.parseBoolean(System.getenv().getOrDefault("REDIS_SSL_ENABLED", "true"));

        RedisURI.Builder builder = RedisURI.Builder.redis(host, port)
                .withAuthentication(username, password.toCharArray());
        if (ssl) {
            builder.withSsl(true);
        }
        RedisURI uri = builder.build();

        try (RedisClient client = RedisClient.create(uri);
             StatefulRedisConnection<String, String> connection = client.connect()) {
            assertEquals("PONG", connection.sync().ping());
        }
    }
}
