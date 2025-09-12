package com.onix.model.users.gateways;

import com.onix.model.login.Login;
import com.onix.model.login.Token;
import com.onix.model.users.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);
    Mono<User> findByEmail(String email);
    Mono<Token> login(Login login);
}
