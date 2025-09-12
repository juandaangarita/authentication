package com.onix.api;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.LoginDTO;
import com.onix.api.dto.UserBatchRequestDTO;
import com.onix.api.mapper.LoginMapper;
import com.onix.api.mapper.UserMapper;
import com.onix.api.validator.LoggingUserValidator;
import com.onix.usecase.authentication.AuthenticationUseCase;
import com.onix.usecase.users.UserUseCase;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
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
    private final TransactionalOperator transactionalOperator;
    private final AuthenticationUseCase authenticationUseCase;
    private final LoginMapper loginMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ServerResponse> listenSaveUser(ServerRequest request) {
        log.trace("Received request to create a new user");
        return request.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.trace("Request body: {}", dto))
                .map(userMapper::toModel)
                .flatMap(user -> loggingUserValidator.validate(user).thenReturn(user))
                .doOnNext(user -> log.debug("User data validated successfully for user: {}", user))
                .flatMap(userUseCase::createUser)
                .as(transactionalOperator::transactional)
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

    public Mono<ServerResponse> listenValidateUser(ServerRequest request) {
        String email = request.queryParam("email").orElseThrow(() ->
                new IllegalArgumentException("Email is required"));
        String documentNumber = request.queryParam("documentNumber").orElseThrow(() ->
                new IllegalArgumentException("Document number is required"));

        log.trace("Received request to validate user with email={} and documentNumber={}", email, documentNumber);

        return userUseCase.isUserRegistered(email, null)
                .as(transactionalOperator::transactional)
                .map(userMapper::toDto)
                .doOnNext(userDTO -> log.debug("User validated successfully: {}", userDTO))
                .flatMap(userDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.OK.value(),
                                "User found",
                                userDTO))
                );

    }

    public Mono<ServerResponse> listenLoginUser(ServerRequest request) {
        return request
                .bodyToMono(LoginDTO.class)
                .doOnNext(dto -> log.trace("Request login for email: {}", dto.email()))
                .map(loginMapper::loginToModel)
                .flatMap(authenticationUseCase::login)
                .map(tokenDTO -> {
                    log.debug("Login successful, generated token: {}", tokenDTO.token());
                    return tokenDTO;
                })
                .map(loginMapper::tokenToDTO)
                .flatMap(tokenDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.OK.value(),
                                "Login successful",
                                tokenDTO))
                );
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ServerResponse> listenGetUsersByEmails(ServerRequest request) {
        log.trace("Received batch request to get users by email");

        return request
                .bodyToMono(UserBatchRequestDTO.class)
                .map(UserBatchRequestDTO::emails)
                .doOnNext(emails -> log.trace("Request body emails: {}", emails))
                .flatMap(userUseCase::getUserByEmails)
                .map(userMap -> userMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> userMapper.toDto(entry.getValue())
                        ))
                )
                .as(transactionalOperator::transactional)
                .doOnNext(userMap -> log.debug("Users retrieved successfully: {}", userMap.keySet()))
                .flatMap(userMap -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.OK.value(),
                                "Users retrieved successfully",
                                userMap))
                );
    }

}
