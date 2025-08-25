package com.onix.usecase.users.config;

import java.math.BigDecimal;

public interface SalaryConfig {
    BigDecimal getMinSalary();
    BigDecimal getMaxSalary();
}
