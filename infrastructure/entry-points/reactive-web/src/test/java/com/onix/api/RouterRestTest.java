package com.onix.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.onix.api.config.AuthenticationConfig;
import com.onix.model.users.User;
import com.onix.usecase.users.UserUseCase;
import java.net.URI;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {RouterRest.class, UserHandler.class})
@EnableConfigurationProperties(AuthenticationConfig.class)
@WebFluxTest
class RouterRestTest {

    private static final String USERS = "/api/v1/users";
    private static final LocalDate BIRTH_DATE = LocalDate.parse("1990-01-01");

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserHandler userHandler;

    @MockitoBean
    private UserUseCase userUseCase;

    @Autowired
    private AuthenticationConfig authConfig;

    @Test
    void shouldLoadAuthenticationProperties() {
        assertEquals(USERS, authConfig.getUsers());
    }


    @Test
    void shouldPostSaveTask() {
        User user = new User().toBuilder()
                .name("Pedro")
                .lastname("Perez")
                .birthDate(BIRTH_DATE)
                .address("Street 123")
                .phone("1234567890")
                .email("email@email.com")
                .baseSalary(3000L)
                .build();

        when(userUseCase.createUser(any())).thenReturn(Mono.just(user));
        when(userHandler.listenSaveUser(any()))
                .thenReturn(ServerResponse.created(URI.create(USERS)).bodyValue("User created"));

        webTestClient.post()
                .uri(USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class);
    }
}
