package com.onix.api.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.onix.api.dto.ApiResponse;
import com.onix.model.users.exception.EmailAlreadyRegisteredException;
import com.onix.model.users.exception.UnregisteredUserException;
import com.onix.model.users.exception.ValidationException;
import com.onix.security.exception.InvalidCredentialsException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GlobalFilterTest {

    @Mock
    private WebFilterChain filterChain;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GlobalFilter globalFilter;

    private ServerWebExchange exchange;
    private MockServerHttpResponse response;

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        exchange = MockServerWebExchange.from(request);
        this.response = (MockServerHttpResponse) exchange.getResponse();
    }

    @Test
    void shouldHandleEmailAlreadyRegisteredException() throws JsonProcessingException {
        // Arrange
        String message = "Email already registered";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new EmailAlreadyRegisteredException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":409,\"error\":\"Validation error\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleValidationException() throws JsonProcessingException {
        // Arrange
        String message = "Validation error message";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new ValidationException(List.of(message))));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":400,\"error\":\"Validation error\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleUnregisteredUserException() throws JsonProcessingException {
        // Arrange
        String message = "Unregistered user";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new UnregisteredUserException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":404,\"error\":\"Not Found\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleIllegalArgumentException() throws JsonProcessingException {
        // Arrange
        String message = "Illegal argument message";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new IllegalArgumentException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":400,\"error\":\"Validation error\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleInvalidCredentialsException() throws JsonProcessingException {
        // Arrange
        String message = "Invalid credentials";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new InvalidCredentialsException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleAuthorizationDeniedException() throws JsonProcessingException {
        // Arrange
        String message = "Authorization denied";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new AuthorizationDeniedException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleServerWebInputException() throws IOException {
        // Arrange
        InvalidFormatException cause = new InvalidFormatException(
                "invalid type", null, ServerRequest.class, new JsonMappingException.Reference("field", "field_name").getClass());
        ServerWebInputException exception = new ServerWebInputException("web input error", null, cause);

        when(filterChain.filter(exchange)).thenReturn(Mono.error(exception));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":400,\"error\":\"Validation error\",\"message\":\"Invalid type for field field_name\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    void shouldHandleUnknownException() throws JsonProcessingException {
        // Arrange
        String message = "Some other error";
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new RuntimeException(message)));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenReturn(("{\"status\":500,\"error\":\"Internal server error\",\"message\":\"" + message + "\"}").getBytes());

        // Act
        Mono<Void> result = globalFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }
}