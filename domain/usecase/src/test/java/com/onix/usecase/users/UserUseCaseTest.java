package com.onix.usecase.users;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.onix.model.users.User;
import com.onix.model.users.gateways.UserRepository;
import com.onix.usecase.users.exception.EmailAlreadyRegisteredException;
import com.onix.usecase.users.exception.ValidationException;
import com.onix.usecase.users.validator.UserValidator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserUseCaseTest {

    private static final LocalDate BIRTH_DATE = LocalDate.parse("1990-01-01");

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserValidator userValidator;
    @InjectMocks
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCreateUserWhenValidAndEmailUnique() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        doReturn(Mono.empty()).when(userValidator).validate(user);
        doReturn(Mono.empty()).when(userRepository).findByEmail(user.getEmail());
        doReturn(Mono.just(user)).when(userRepository).saveUser(user);

        StepVerifier.create(userUseCase.createUser(user))
                .expectNext(user)
                .verifyComplete();

        verify(userValidator).validate(user);
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).saveUser(user);
    }

    @Test
    void shouldFailWhenUserInvalid() {
        User user = new User().toBuilder()
                .name("")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        doReturn(Mono.error(new ValidationException(List.of("Name cannot be null or empty"))))
                .when(userValidator).validate(user);
        doReturn(Mono.empty()).when(userRepository).findByEmail(any());
        doReturn(Mono.empty()).when(userRepository).saveUser(any());

        StepVerifier.create(userUseCase.createUser(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Name cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldFailWhenEmailAlreadyRegistered() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        User existingUser = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        doReturn(Mono.empty()).when(userValidator).validate(user);
        doReturn(Mono.just(existingUser)).when(userRepository).findByEmail(user.getEmail());
        doReturn(Mono.empty()).when(userRepository).saveUser(any());

        StepVerifier.create(userUseCase.createUser(user))
                .expectErrorMatches(ex -> ex instanceof EmailAlreadyRegisteredException &&
                        ex.getMessage().contains(user.getEmail()))
                .verify();
    }
}