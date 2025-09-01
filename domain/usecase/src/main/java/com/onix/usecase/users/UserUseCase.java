package com.onix.usecase.users;

import com.onix.model.users.User;
import com.onix.model.users.gateways.UserRepository;
import com.onix.model.users.exception.EmailAlreadyRegisteredException;
import com.onix.usecase.users.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public Mono<User> createUser(User user) {
        return userValidator.validate(user)
                .then(validateEmailUnique(user))
                .then(userRepository.saveUser(user));
    }

    private Mono<Void> validateEmailUnique(User user) {
        return userRepository.findByEmail(user.getEmail())
                .flatMap(existing -> Mono.<Void>error(new EmailAlreadyRegisteredException(user.getEmail())))
                .then();
    }

    public Mono<User> isUserRegistered(String email, String documentNumber) {
        return userRepository.findByEmail(email);
    }
}
