//package com.onix.api.config;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//
//import com.onix.api.UserHandler;
//import com.onix.api.RouterRest;
//import com.onix.api.mapper.UserMapper;
//import com.onix.api.validator.LoggingUserValidator;
//import com.onix.usecase.authentication.AuthenticationUseCase;
//import com.onix.usecase.users.UserUseCase;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.springframework.transaction.reactive.TransactionalOperator;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.ServerResponse;
//
//@ContextConfiguration(classes = {RouterRest.class, UserHandler.class})
//@WebFluxTest
//@Import({CorsConfig.class, SecurityHeadersConfig.class})
//class ConfigTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockitoBean
//    private UserHandler userHandler;
//
//    @MockitoBean
//    private UserUseCase userUseCase;
//
//    @MockitoBean
//    private UserMapper userMapper;
//
//    @MockitoBean
//    private LoggingUserValidator loggingUserValidator;
//
//    @MockitoBean
//    private TransactionalOperator transactionalOperator;
//
//    @MockitoBean
//    private AuthenticationUseCase authenticationUseCase;
//
//    @MockitoBean
//    private AuthenticationConfig authenticationConfig;
//
//    // Static block to configure the mock before the context is loaded
//    static {
//        AuthenticationConfig authConfigMock = mock(AuthenticationConfig.class);
//        when(authConfigMock.getUsers()).thenReturn("/mock/users");
//        when(authConfigMock.getBatch()).thenReturn("/mock/batch");
//        when(authConfigMock.getLogin()).thenReturn("/mock/login");
//        when(authConfigMock.getValidate()).thenReturn("/mock/validate");
//    }
//
//    @TestConfiguration
//    static class TestConfig {
////        @Bean
////        public AuthenticationConfig authenticationConfig() {
////            AuthenticationConfig authConfigMock = mock(AuthenticationConfig.class);
////            when(authConfigMock.getUsers()).thenReturn("/mock/users");
////            when(authConfigMock.getBatch()).thenReturn("/mock/batch");
////            when(authConfigMock.getLogin()).thenReturn("/mock/login");
////            when(authConfigMock.getValidate()).thenReturn("/mock/validate");
////            return authConfigMock;
////        }
//
//        @Bean
//        public RouterFunction<ServerResponse> usersOpenApi() {
//            return route().build();
//        }
//
//        @Bean
//        public RouterFunction<ServerResponse> authenticateOpenApi() {
//            return route().build();
//        }
//    }
//
//    @Test
//    void corsConfigurationShouldAllowOrigins() {
//        webTestClient.get()
//                .uri("/api/usecase/path")
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().valueEquals("Content-Security-Policy",
//                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
//                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
//                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
//                .expectHeader().valueEquals("Server", "")
//                .expectHeader().valueEquals("Cache-Control", "no-store")
//                .expectHeader().valueEquals("Pragma", "no-cache")
//                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
//    }
//
//}