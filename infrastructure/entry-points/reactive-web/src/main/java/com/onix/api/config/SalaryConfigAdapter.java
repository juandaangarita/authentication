package com.onix.api.config;

import com.onix.usecase.users.config.SalaryConfig;
import java.math.BigDecimal;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Generated
@Configuration
@ConfigurationProperties(prefix = "authentication.salary")
@Getter
@Setter
public class SalaryConfigAdapter implements SalaryConfig {

    private BigDecimal min;
    private BigDecimal max;

    @Override
    public BigDecimal getMinSalary() {
        return min;
    }

    @Override
    public BigDecimal getMaxSalary() {
        return max;
    }
}