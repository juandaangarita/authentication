package com.onix.usecase.users.validator;

import static org.mockito.Mockito.lenient;

import com.onix.model.users.User;
import com.onix.usecase.users.config.SalaryConfig;
import com.onix.model.users.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserValidatorTest {

    private static final LocalDate BIRTH_DATE = LocalDate.parse("1990-01-01");

    @Mock
    private SalaryConfig salaryConfig;
    @InjectMocks
    private UserValidator validator;

    @BeforeEach
    void setUp() {
        lenient().when(salaryConfig.getMinSalary()).thenReturn(BigDecimal.valueOf(0));
        lenient().when(salaryConfig.getMaxSalary()).thenReturn(BigDecimal.valueOf(15000000));
    }

    @Test
    void shouldPassValidationForValidUser() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenNameIsEmpty() {
        User user = new User().toBuilder()
                .name("")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Name cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenNameIsNull() {
        User user = new User().toBuilder()
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Name cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenLastNameIsEmpty() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Lastname cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenLastNameIsNull() {
        User user = new User().toBuilder()
                .name("Pedro")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Lastname cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenSalaryIsNull() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Base salary cannot be null"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenSalaryOutOfRange() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(60000000L)
                .build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().stream()
                                .anyMatch(msg -> msg.contains("Base salary must be between")))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenSalaryOutOfRangeNegative() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(-60000000L)
                .build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().stream()
                                .anyMatch(msg -> msg.contains("Base salary must be between")))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenEmailIsNull() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Email cannot be null"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenEmailIsInvalid() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("invalid-email")
                .baseSalary(3000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Email format is invalid"))
                .verify();
    }


    @Test
    void shouldReturnMultipleErrors() {
        User user = new User().toBuilder()
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("invalid-email")
                .baseSalary(60000000L).build();

        StepVerifier.create(validator.validate(user))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().size() == 4)
                .verify();
    }

}