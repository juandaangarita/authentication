package com.onix.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.UserDTO;
import com.onix.api.maper.UserMapper;
import com.onix.model.users.User;
import com.onix.usecase.users.UserUseCase;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(UserHandler.class)
class UserHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AuthenticationConfig authConfig;

    @Test
    void listenSaveUser_returnsCreated() {
        CreateUserDTO dto = new CreateUserDTO("Juan", "Angarita", LocalDate.of(1990,1,1),
                "Calle 123", "5551234", "juan@example.com", 5000L);

        User user = User.builder().userId(UUID.randomUUID()).name("Juan").build();
        UserDTO responseDTO = new UserDTO(user.getUserId(), "Juan", "Angarita");

        when(userMapper.toModel(dto)).thenReturn(user);
        when(userUseCase.createUser(user)).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(responseDTO);
        when(authConfig.getUsers()).thenReturn("/users/");

        webTestClient.post()
                .uri("/users")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.userId").isEqualTo(responseDTO.userId().toString());
    }
}