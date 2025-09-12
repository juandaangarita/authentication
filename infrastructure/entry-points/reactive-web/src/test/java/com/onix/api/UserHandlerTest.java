package com.onix.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.LoginDTO;
import com.onix.api.dto.TokenDTO;
import com.onix.api.dto.UserBatchRequestDTO;
import com.onix.api.dto.UserDTO;
import com.onix.api.mapper.LoginMapper;
import com.onix.api.mapper.UserMapper;
import com.onix.api.validator.LoggingUserValidator;
import com.onix.model.login.Login;
import com.onix.model.login.Token;
import com.onix.model.users.User;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.usecase.authentication.AuthenticationUseCase;
import com.onix.usecase.users.UserUseCase;
import com.onix.model.users.exception.ValidationException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.transaction.reactive.TransactionalOperator;
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

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private AuthenticationUseCase authenticationUseCase;
    @Mock
    private LoginMapper loginMapper;

    private User user;
    private UserDTO userDTO;
    private CreateUserDTO createUserDTO;
    private Token token;
    private Login login;
    private TokenDTO tokenDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setup() {
        createUserDTO = new CreateUserDTO("Pedro", "Perez", LocalDate.of(1990,1,1),
                "Street 123", "1234567890", "email@email.com", "123", 3000L,
                "paswword", 1);

        user = User.builder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(LocalDate.of(1990,1,1))
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        userDTO = new UserDTO(UUID.randomUUID(), "Pedro", "Perez", LocalDate.of(1990,1,1),"", "", "email@email.com", "123", 3000L);

        login = new Login("email@email.com", "password");
        token = new Token("mockedToken");
        loginDTO = new LoginDTO("email@email.com", "password");
        tokenDTO = new TokenDTO("mockedToken");

        lenient().when(userMapper.toModel(any())).thenReturn(user);
        lenient().when(userMapper.toDto(any())).thenReturn(userDTO);
        lenient().when(loginMapper.loginToModel(any())).thenReturn(login);
        lenient().when(loginMapper.tokenToDTO(any())).thenReturn(tokenDTO);
        lenient().when(loggingUserValidator.validate(any())).thenReturn(Mono.empty());
        lenient().when(userUseCase.createUser(any())).thenReturn(Mono.just(user));
        lenient().when(authConfig.getUsers()).thenReturn("/api/v1/users/");
        lenient().when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(authenticationUseCase.login(any())).thenReturn(Mono.just(token));
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
                "Street 123", "1234567890", "email@email.com", "123", 3000L,
                "paswword", 1);

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

    @Test
    void shouldValidateUserSuccessfully() {
        // Arrange
        when(userUseCase.isUserRegistered(anyString(), any())).thenReturn(Mono.just(user));

        ServerRequest request = MockServerRequest.builder()
                .queryParam("email", "test@email.com")
                .queryParam("documentNumber", "12345")
                .build();

        // Act
        Mono<ServerResponse> responseMono = userHandler.listenValidateUser(request);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWithoutEmail() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .queryParam("documentNumber", "12345")
                .build();

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userHandler.listenValidateUser(request);
        });
    }

    @Test
    void shouldLoginUserSuccessfully() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(loginDTO));

        // Act
        Mono<ServerResponse> responseMono = userHandler.listenLoginUser(request);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    void shouldFailLoginUser() {
        // Arrange
        when(authenticationUseCase.login(any(Login.class))).thenReturn(Mono.error(new InvalidCredentialsException("Invalid")));
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(loginDTO));

        // Act & Assert
        StepVerifier.create(userHandler.listenLoginUser(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(InvalidCredentialsException.class);
                    assertThat(error.getMessage()).contains("Invalid");
                })
                .verify();
    }

    @Test
    void shouldGetUsersByEmailsSuccessfully() {
        // Arrange
        UserBatchRequestDTO userBatchRequestDTO = new UserBatchRequestDTO(Set.of("email@email.com"));
        Map<String, User> userMap = Map.of("email@email.com", user);

        when(userUseCase.getUserByEmails(any())).thenReturn(Mono.just(userMap));

        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(userBatchRequestDTO));

        // Act
        Mono<ServerResponse> responseMono = userHandler.listenGetUsersByEmails(request);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldGetEmptyUsersByEmails() {
        // Arrange
        UserBatchRequestDTO userBatchRequestDTO = new UserBatchRequestDTO(Set.of());
        Map<String, User> emptyMap = Map.of();

        when(userUseCase.getUserByEmails(any())).thenReturn(Mono.just(emptyMap));

        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(userBatchRequestDTO));

        // Act
        Mono<ServerResponse> responseMono = userHandler.listenGetUsersByEmails(request);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }
}