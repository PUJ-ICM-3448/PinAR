package com.pinar.comunitiesservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RedisStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(RedisStartupLogger.class);

    private final String host;
    private final int port;
    private final String username;
    private final boolean ssl;
    private final String password;

    public RedisStartupLogger(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.username:}") String username,
            @Value("${spring.data.redis.ssl.enabled:false}") boolean ssl,
            @Value("${spring.data.redis.password:}") String password
    ) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.ssl = ssl;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logRedisTarget() {
        boolean passwordSet = StringUtils.hasText(password);
        log.info(
                "Redis configurado: host={} port={} username={} ssl={} passwordSet={}",
                host, port, username, ssl, passwordSet
        );
        if ("localhost".equals(host) && !passwordSet) {
            log.warn(
                    "Parece la config por defecto (localhost sin password). "
                            + "Crear application-local.properties"
            );
        }
    }
}
