package com.onix.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "authentication.paths")
public class AuthenticationConfig {
    private String base;
    private String users;

    public String getUsers() {
        return base + users;
    }
}
