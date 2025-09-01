package com.onix.usecase.users.validator;

import com.onix.model.users.User;
import com.onix.usecase.users.config.SalaryConfig;
import com.onix.model.users.exception.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserValidator {

    private final SalaryConfig salaryConfig;

    public Mono<Void> validate(User user) {
        List<String> errors = new ArrayList<>();
        validateName(user, errors);
        validateLastname(user, errors);
        validateBaseSalary(user, errors);
        validateEmail(user, errors);

        if (!errors.isEmpty()) {
            return Mono.error(new ValidationException(errors));
        }

        return Mono.empty();
    }

    private void validateName(User user, List<String> errors) {
        if (isNullOrEmpty(user.getName())) {
            errors.add("Name cannot be null or empty");
        }
    }

    private void validateLastname(User user, List<String> errors) {
        if (isNullOrEmpty(user.getLastname())) {
            errors.add("Lastname cannot be null or empty");
        }
    }

    private void validateBaseSalary(User user, List<String> errors) {
        if (user.getBaseSalary() == null) {
            errors.add("Base salary cannot be null");
        } else {
            BigDecimal salary = BigDecimal.valueOf(user.getBaseSalary());
            if (salary.compareTo(salaryConfig.getMinSalary()) < 0 ||
                    salary.compareTo(salaryConfig.getMaxSalary()) > 0) {
                errors.add(String.format("Base salary must be between %s and %s",
                        salaryConfig.getMinSalary(), salaryConfig.getMaxSalary()));
            }
        }
    }

    private void validateEmail(User user, List<String> errors) {
        if (user.getEmail() == null) {
            errors.add("Email cannot be null");
        } else if (!isValidEmail(user.getEmail())) {
            errors.add("Email format is invalid");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$");
    }
}
