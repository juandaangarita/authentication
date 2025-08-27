package com.onix.api;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateUserDTO;
import com.onix.api.mapper.UserMapper;
import com.onix.api.validator.LoggingUserValidator;
import com.onix.usecase.users.UserUseCase;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHandler {

    private final UserUseCase userUseCase;
    private final AuthenticationConfig authConfig;
    private final UserMapper userMapper;
    private final LoggingUserValidator loggingUserValidator;

    public Mono<ServerResponse> listenSaveUser(ServerRequest request) {
        log.trace("Received request to create a new user");
        return request.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.trace("Request body: {}", dto))
                .map(userMapper::toModel)
                .flatMap(user -> loggingUserValidator.validate(user).thenReturn(user))
                .flatMap(userUseCase::createUser)
                .map(userMapper::toDto)
                .doOnNext(userDTO -> log.debug("User created successfully with ID: {}", userDTO.userId()))
                .flatMap(userDTO -> ServerResponse
                        .created(URI.create(authConfig.getUsers() + userDTO.userId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "User created successfully",
                                userDTO))
                );
    }

}
