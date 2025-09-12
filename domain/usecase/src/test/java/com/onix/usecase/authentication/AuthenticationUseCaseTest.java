package com.onix.usecase.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.login.Login;
import com.onix.model.login.Token;
import com.onix.model.users.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationUseCase authenticationUseCase;

    @Test
    void testLogin() {
        Token token = new Token("token");
        when(userRepository.login(any())).thenReturn(Mono.just(new Token("token")));

        StepVerifier.create(authenticationUseCase.login(new Login("email", "password")))
            .expectNext(token)
            .verifyComplete();

        verify(userRepository).login(any());
    }

}