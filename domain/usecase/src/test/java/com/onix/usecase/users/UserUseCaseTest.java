package com.onix.usecase.users;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.onix.model.users.User;
import com.onix.model.users.gateways.UserRepository;
import com.onix.model.users.exception.EmailAlreadyRegisteredException;
import com.onix.model.users.exception.ValidationException;
import com.onix.usecase.users.validator.UserValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Test
    void shouldReturnUserWhenRegistered() {
        // Arrange
        String email = "existing@email.com";
        String documentNumber = "1234567890";
        User user = User.builder().email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Mono.just(user));

        // Act & Assert
        StepVerifier.create(userUseCase.isUserRegistered(email, documentNumber))
                .expectNext(user)
                .verifyComplete();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldReturnEmptyWhenUserNotRegistered() {
        // Arrange
        String email = "nonexistent@email.com";
        String documentNumber = "0987654321";

        when(userRepository.findByEmail(email)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userUseCase.isUserRegistered(email, documentNumber))
                .expectNextCount(0)
                .verifyComplete();
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetUsersByEmails() {
        // Arrange
        Set<String> emails = Set.of("user1@email.com", "user2@email.com");
        User user1 = User.builder().email("user1@email.com").build();
        User user2 = User.builder().email("user2@email.com").build();

        when(userRepository.findByEmail("user1@email.com")).thenReturn(Mono.just(user1));
        when(userRepository.findByEmail("user2@email.com")).thenReturn(Mono.just(user2));

        // Act & Assert
        StepVerifier.create(userUseCase.getUserByEmails(emails))
                .expectNextMatches(result -> {
                    assertEquals(2, result.size());
                    assertEquals(user1, result.get("user1@email.com"));
                    assertEquals(user2, result.get("user2@email.com"));
                    return true;
                })
                .verifyComplete();

        verify(userRepository).findByEmail("user1@email.com");
        verify(userRepository).findByEmail("user2@email.com");
    }

    @Test
    void shouldReturnEmptyMapWhenEmailsAreEmpty() {
        // Arrange
        Set<String> emails = Set.of();

        // Act & Assert
        StepVerifier.create(userUseCase.getUserByEmails(emails))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();

        verifyNoInteractions(userRepository);
    }
}