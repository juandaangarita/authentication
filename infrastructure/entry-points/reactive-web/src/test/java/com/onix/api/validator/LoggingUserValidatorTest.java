package com.onix.api.validator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.users.User;
import com.onix.model.users.exception.ValidationException;
import com.onix.usecase.users.validator.UserValidator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoggingUserValidatorTest {

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private LoggingUserValidator loggingUserValidator;

    @Test
    void shouldPassWhenValidatorSucceeds() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .email("pedro@email.com")
                .build();

        when(userValidator.validate(user)).thenReturn(Mono.empty());

        StepVerifier.create(loggingUserValidator.validate(user))
                .verifyComplete();

        verify(userValidator).validate(user);
    }

    @Test
    void shouldFailWhenValidatorFails() {
        User user = new User().toBuilder()
                .name("")
                .lastname("Perez")
                .email("pedro@email.com")
                .build();

        when(userValidator.validate(user))
                .thenReturn(Mono.error(new ValidationException(List.of("Name cannot be null or empty"))));

        StepVerifier.create(loggingUserValidator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Name cannot be null or empty"))
                .verify();

        verify(userValidator).validate(user);
    }
}