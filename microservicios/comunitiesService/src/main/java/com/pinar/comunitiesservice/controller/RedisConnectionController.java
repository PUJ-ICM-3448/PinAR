package com.pinar.comunitiesservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/test/redis")
public class RedisConnectionController {

    private final RedisConnectionFactory redisConnectionFactory;
    private final String redisHost;
    private final int redisPort;
    private final String redisUsername;
    private final boolean redisSsl;
    private final boolean passwordConfigured;
    private final boolean usingRedisUrl;

    public RedisConnectionController(
            RedisConnectionFactory redisConnectionFactory,
            @Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") int redisPort,
            @Value("${spring.data.redis.username:}") String redisUsername,
            @Value("${spring.data.redis.ssl.enabled:false}") boolean redisSsl,
            @Value("${spring.data.redis.password:}") String redisPassword,
            @Value("${spring.data.redis.url:}") String redisUrl
    ) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisUsername = redisUsername;
        this.redisSsl = redisSsl;
        this.passwordConfigured = StringUtils.hasText(redisPassword);
        this.usingRedisUrl = StringUtils.hasText(redisUrl);
    }

    @GetMapping("/check-redis")
    public Map<String, Object> checkRedisConnection() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("host", redisHost);
        result.put("port", redisPort);
        result.put("username", redisUsername);
        result.put("ssl", redisSsl);
        result.put("passwordConfigured", passwordConfigured);
        result.put("usingRedisUrl", usingRedisUrl);

        if ("localhost".equals(redisHost) && !passwordConfigured) {
            result.put("connected", false);
            result.put("message", "Spring está usando localhost:6379 (valores por defecto).");
            result.put("fix", "Las variables REDIS_* no llegaron al proceso. Opción A: copia "
                    + "application-local.properties.example a application-local.properties con tus datos. "
                    + "Opción B: en IntelliJ Run Configuration añade REDIS_HOST, REDIS_PORT, REDIS_PASSWORD, "
                    + "REDIS_USERNAME=default, REDIS_SSL_ENABLED=false y reinicia.");
            return result;
        }

        if (isRedisCloudHost(redisHost) && !StringUtils.hasText(redisUsername)) {
            result.put("connected", false);
            result.put("message", "Redis Cloud requiere usuario ACL.");
            result.put("fix", "Define REDIS_USERNAME=default (o el usuario de tu base de datos).");
            return result;
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            result.put("connected", pong != null);
            result.put("ping", pong);
            result.put("message", "Redis responde correctamente.");
        } catch (Exception e) {
            result.put("connected", false);
            result.put("message", e.getMessage());
            result.put("exception", e.getClass().getSimpleName());
            Throwable cause = e.getCause();
            if (cause != null) {
                result.put("cause", cause.getMessage());
            }
            if (isRedisCloudHost(redisHost)) {
                result.put("fix", "En Redis Cloud revisa si tu puerto es TLS o no. "
                        + "Si el puerto es sin TLS, usa REDIS_SSL_ENABLED=false. "
                        + "Si es TLS, usa REDIS_SSL_ENABLED=true. "
                        + "Usuario ACL: REDIS_USERNAME=default. Reinicia el microservicio tras cambiar variables.");
            }
            if (e.getMessage() != null && e.getMessage().contains("NotSslRecord")) {
                result.put("fix", "El puerto parece ser sin TLS: prueba REDIS_SSL_ENABLED=false. "
                        + "Si el panel indica TLS, usa el puerto TLS y REDIS_SSL_ENABLED=true.");
            }
        }
        return result;
    }

    private static boolean isRedisCloudHost(String host) {
        return host != null && host.contains("redis.io");
    }
}
