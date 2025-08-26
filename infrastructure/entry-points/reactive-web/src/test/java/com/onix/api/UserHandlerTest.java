package com.onix.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.UserDTO;
import com.onix.api.maper.UserMapper;
import com.onix.api.validator.LoggingUserValidator;
import com.onix.model.users.User;
import com.onix.usecase.users.UserUseCase;
import com.onix.usecase.users.exception.ValidationException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @InjectMocks
    private UserHandler userHandler;

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private AuthenticationConfig authConfig;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoggingUserValidator loggingUserValidator;

    private User user;
    private UserDTO userDTO;
    private CreateUserDTO createUserDTO;

    @BeforeEach
    void setup() {
        createUserDTO = new CreateUserDTO("Pedro", "Perez", LocalDate.of(1990,1,1),
                "Street 123", "1234567890", "email@email.com", 3000L);

        user = User.builder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(LocalDate.of(1990,1,1))
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        userDTO = new UserDTO(UUID.randomUUID(), "Pedro", "Perez", LocalDate.of(1990,1,1),"", "", "email@email.com", 3000L);

        lenient().when(userMapper.toModel(any())).thenReturn(user);
        lenient().when(userMapper.toDto(any())).thenReturn(userDTO);
        lenient().when(loggingUserValidator.validate(any())).thenReturn(Mono.empty());
        lenient().when(userUseCase.createUser(any())).thenReturn(Mono.just(user));
        lenient().when(authConfig.getUsers()).thenReturn("/api/v1/users/");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/api/v1/users"))
                .body(Mono.just(createUserDTO));

        Mono<ServerResponse> responseMono = userHandler.listenSaveUser(request);

        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> assertEquals(HttpStatus.CREATED, serverResponse.statusCode()))
                .verifyComplete();
    }

    @Test
    void shouldNotCreateUser() {
        when(loggingUserValidator.validate(any()))
                .thenReturn(Mono.error(new ValidationException(List.of("Invalid user"))));

        CreateUserDTO createInvalidUserDTO = new CreateUserDTO("", "Perez", LocalDate.of(1990,1,1),
                "Street 123", "1234567890", "email@email.com", 3000L);

        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/api/v1/users"))
                .body(Mono.just(createInvalidUserDTO));

        Mono<ServerResponse> responseMono = userHandler.listenSaveUser(request);

        StepVerifier.create(responseMono)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ValidationException.class);
                    assertThat(error.getMessage()).contains("Invalid user");
                })
                .verify();
    }
}