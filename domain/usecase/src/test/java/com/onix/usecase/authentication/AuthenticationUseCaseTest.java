package com.onix.usecase.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
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
        TokenDTO tokenDTO = new TokenDTO("token");
        when(userRepository.login(any())).thenReturn(Mono.just(new TokenDTO("token")));

        StepVerifier.create(authenticationUseCase.login(new LoginDTO("email", "password")))
            .expectNext(tokenDTO)
            .verifyComplete();

        verify(userRepository).login(any());
    }

}