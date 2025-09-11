package com.onix.api.config;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated
@Getter
@Setter
@ConfigurationProperties(prefix = "authentication.paths")
public class AuthenticationConfig {
    private String base;
    private String users;
    private String validate;
    private String login;
    private String batch;

    public String getUsers() {
        return base + users;
    }

    public String getValidate() {
        return base + users + validate;
    }

    public String getLogin() {
        return base + login;
    }

    public String getBatch() {
        return base +users + batch;
    }
}
