package com.onix.model.users.gateways;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
import com.onix.model.users.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);
    Mono<User> findByEmail(String email);
    Mono<TokenDTO> login(LoginDTO login);
}
