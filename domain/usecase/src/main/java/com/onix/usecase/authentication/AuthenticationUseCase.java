package com.onix.usecase.authentication;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
import com.onix.model.users.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationUseCase {
    private final UserRepository userRepository;

    public Mono<TokenDTO> login(LoginDTO login) {
        return userRepository.login(login);
    }
}
