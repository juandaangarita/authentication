package com.onix.r2dbc.config;

import lombok.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated
@ConfigurationProperties(prefix = "adapters.r2dbc.postgres")
public record PostgresqlConnectionProperties(
        String host,
        Integer port,
        String database,
        String schema,
        String username,
        String password) {
}
