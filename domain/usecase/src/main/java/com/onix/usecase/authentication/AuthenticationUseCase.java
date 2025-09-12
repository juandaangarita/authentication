package com.onix.usecase.authentication;

import com.onix.model.login.Login;
import com.onix.model.login.Token;
import com.onix.model.users.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthenticationUseCase {
    private final UserRepository userRepository;

    public Mono<Token> login(Login login) {
        return userRepository.login(login);
    }
}
