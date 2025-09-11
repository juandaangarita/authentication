package com.onix.security.jwt;

import com.onix.security.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private WebFilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        // Mocking the behavior of the chain to return a completed Mono
        lenient().when(filterChain.filter(any(MockServerWebExchange.class)))
                .thenReturn(Mono.empty());
    }

    @Test
    void shouldPassFilterForWhitelistedPath() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        // Verify that the filter chain continued without an exception or attribute modification
        verify(filterChain).filter(exchange);
        assertNull(exchange.getAttribute("token"));
    }

    @Test
    void shouldExtractTokenAndContinueForValidAuthorization() {
        // Arrange
        String token = "valid-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        // Verify that the token was put into the exchange attributes and the filter chain continued
        assertEquals(token, exchange.getAttribute("token"));
        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldFailForMissingAuthorizationHeader() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected/resource").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        try {
            jwtFilter.filter(exchange, filterChain);
            // This line should not be reached if an exception is thrown
            StepVerifier.create(Mono.empty()).expectError().verify();
        } catch (InvalidCredentialsException ex) {
            StepVerifier.create(Mono.error(ex))
                    .expectErrorMatches(e -> e instanceof InvalidCredentialsException &&
                            e.getMessage().equals("Invalid credentials. No token was found"))
                    .verify();
        }
    }

    @Test
    void shouldFailForInvalidAuthorizationScheme() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected/resource")
                .header(HttpHeaders.AUTHORIZATION, "Basic invalid-scheme")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        try {
            jwtFilter.filter(exchange, filterChain);
            // This line should not be reached if an exception is thrown
            StepVerifier.create(Mono.empty()).expectError().verify();
        } catch (InvalidCredentialsException ex) {
            StepVerifier.create(Mono.error(ex))
                    .expectErrorMatches(e -> e instanceof InvalidCredentialsException &&
                            e.getMessage().equals("Invalid credentials. Invalid auth"))
                    .verify();
        }
    }
}