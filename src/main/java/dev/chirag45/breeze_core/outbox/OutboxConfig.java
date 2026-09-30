package dev.chirag45.breeze_core.outbox;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxConfig {

    @Bean(destroyMethod = "close")
    ThreadPoolBulkhead outboxBulkhead(OutboxProperties properties) {
        var config = ThreadPoolBulkheadConfig.custom()
                .coreThreadPoolSize(properties.getWorkerThreads())
                .maxThreadPoolSize(properties.getWorkerThreads())
                .queueCapacity(0)
                .build();
        return ThreadPoolBulkhead.of("outbox-profile-sync", config);
    }
}
