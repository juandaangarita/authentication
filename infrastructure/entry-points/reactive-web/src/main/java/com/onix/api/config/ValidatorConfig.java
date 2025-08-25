package com.onix.api.config;

import com.onix.usecase.users.config.SalaryConfig;
import com.onix.usecase.users.validator.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public UserValidator userValidator(SalaryConfig salaryConfig) {
        return new UserValidator(salaryConfig);
    }
}