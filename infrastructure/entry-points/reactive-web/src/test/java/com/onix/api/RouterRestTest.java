package com.onix.api;

import static org.mockito.ArgumentMatchers.any;
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
import com.onix.security.config.JwtConfigProperties;
import com.onix.security.config.SecurityConfig;
import com.onix.security.jwt.JwtAuthenticationManager;
import com.onix.security.jwt.JwtFilter;
import com.onix.security.jwt.JwtProvider;
import com.onix.security.repository.SecurityContextRepository;
import com.onix.usecase.authentication.AuthenticationUseCase;
import com.onix.usecase.users.UserUseCase;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {RouterRest.class, UserHandler.class})
@EnableConfigurationProperties(AuthenticationConfig.class)
@WebFluxTest
@Import(SecurityConfig.class)
class RouterRestTest {

    private static final String USERS = "/api/v1/users";
    private static final String BATCH = "/api/v1/users/batch";
    private static final String LOGIN = "/api/v1/login";
    private static final String VALIDATE = "/api/v1/users/validate";
    private static final LocalDate BIRTH_DATE = LocalDate.parse("1990-01-01");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthenticationConfig authConfig;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private AuthenticationUseCase authenticationUseCase;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private LoginMapper loginMapper;

    @MockitoBean
    private LoggingUserValidator loggingUserValidator;

    @MockitoBean
    private TransactionalOperator transactionalOperator;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtConfigProperties jwtConfigProperties;

    @BeforeEach
    void setUp() {
        when(jwtFilter.filter(any(), any())).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0);
            WebFilterChain chain = invocation.getArgument(1);
            return chain.filter(exchange);
        });
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldCreateUser() {
        CreateUserDTO dto = new CreateUserDTO(
                "Pedro",
                "Perez",
                BIRTH_DATE,
                "Street 123",
                "1234567890",
                "email@email.com",
                "123",
                3000L,
                "password",
                1);
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .email("email@email.com")
                .baseSalary(3000L)
                .build();
        UserDTO userDTO = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                BIRTH_DATE,
                "Street 123",
                "1234567890",
                "email@email.com",
                "1234",
                3000L);

        when(userMapper.toModel(any())).thenReturn(user);
        when(loggingUserValidator.validate(any())).thenReturn(Mono.empty());
        when(userUseCase.createUser(any())).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(userDTO);
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

        webTestClient.post()
                .uri(USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(201)
                .jsonPath("$.data.name").isEqualTo("Pedro");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldValidateUser() {
        User user = new User().toBuilder().email("email@email.com").documentNumber("1234").build();
        UserDTO userDTO = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                BIRTH_DATE,
                "Street 123",
                "1234567890",
                "email@email.com",
                "1234",
                3000L);

        when(userUseCase.isUserRegistered(any(), any())).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(userDTO);
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(VALIDATE)
                        .queryParam("email", "email@email.com")
                        .queryParam("documentNumber", "1234")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(200)
                .jsonPath("$.data.email").isEqualTo("email@email.com");
    }

    @Test
    void shouldLoginUser() {
        LoginDTO loginDTO = new LoginDTO("email@email.com", "password");
        Login login = new Login("email@email.com", "password");
        TokenDTO tokenDTO = new TokenDTO("sample-token");
        Token token = new Token("sample-token");

        when(authenticationUseCase.login(any())).thenReturn(Mono.just(token));
        when(loginMapper.loginToModel(any())).thenReturn(login);
        when(loginMapper.tokenToDTO(any())).thenReturn(tokenDTO);

        webTestClient.post()
                .uri(LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(200)
                .jsonPath("$.data.token").isEqualTo("sample-token");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldGetUsersByEmails() {
        UserBatchRequestDTO batchRequest = new UserBatchRequestDTO(Set.of("email@email.com"));
        User user = new User().toBuilder().email("email@email.com").build();
        UserDTO userDTO = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                BIRTH_DATE,
                "Street 123",
                "1234567890",
                "email@email.com",
                "1234",
                3000L);

        when(userUseCase.getUserByEmails(any())).thenReturn(Mono.just(Map.of("email@email.com", user)));
        when(userMapper.toDto(user)).thenReturn(userDTO);
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));

        webTestClient.post()
                .uri(BATCH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(200)
                .jsonPath("$.data['email@email.com'].email").isEqualTo("email@email.com");
    }

    @Test
    void shouldRejectWhenNoAdminRole() {
        webTestClient.post()
                .uri(USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CreateUserDTO(
                        "Pedro",
                        "Perez",
                        BIRTH_DATE,
                        "Street 123",
                        "1234567890",
                        "email@email.com",
                        "123",
                        3000L,
                        "password",
                        1))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
