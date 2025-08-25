package com.onix.api.validator;

import com.onix.model.users.User;
import com.onix.usecase.users.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingUserValidator {

    private final UserValidator validator;

    public Mono<Void> validate(User user) {
        log.trace("Validating user: {}", user.toString());
        return validator.validate(user)
                .doOnError(e -> log.trace("Validation failed: {}", e.getMessage()))
                .doOnSuccess(v -> log.trace("Validation succeeded for user: {}", user));
    }
}
