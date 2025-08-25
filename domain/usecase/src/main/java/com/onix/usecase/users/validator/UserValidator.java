package com.onix.usecase.users.validator;

import com.onix.model.users.User;
import com.onix.usecase.users.config.SalaryConfig;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserValidator {

    private final SalaryConfig salaryConfig;

    public Mono<Void> validate(User user) {
        if (isNullOrEmpty(user.getName())) {
            return Mono.error(new IllegalArgumentException("Name cannot be null or empty."));
        }
        if (isNullOrEmpty(user.getLastname())) {
            return Mono.error(new IllegalArgumentException("Lastname cannot be null or empty."));
        }
        if (user.getBaseSalary() == null) {
            return Mono.error(new IllegalArgumentException("Base salary cannot be null."));
        }
        BigDecimal salary = BigDecimal.valueOf(user.getBaseSalary());
        if (salary.compareTo(salaryConfig.getMinSalary()) < 0 ||
                salary.compareTo(salaryConfig.getMaxSalary()) > 0) {
            return Mono.error(new IllegalArgumentException(
                    String.format("Base salary must be between %s and %s.",
                            salaryConfig.getMinSalary(), salaryConfig.getMaxSalary())));
        }
        if (user.getEmail() == null) {
            return Mono.error(new IllegalArgumentException("Email cannot be null."));
        }
        if (!isValidEmail(user.getEmail())) {
            return Mono.error(new IllegalArgumentException("Email format is invalid."));
        }
        return Mono.empty();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$");
    }
}
