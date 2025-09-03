package com.onix.api;

import com.onix.api.config.AuthenticationConfig;
import com.onix.api.openapi.AuthenticateOpenApi;
import com.onix.api.openapi.UserOpenApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final AuthenticationConfig authConfig;
    private final UserHandler userHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return route()
                .POST(authConfig.getUsers(), userHandler::listenSaveUser, UserOpenApi::createUser)
                .POST(authConfig.getLogin(), userHandler::listenLoginUser, AuthenticateOpenApi::authenticateUser)
                .GET(authConfig.getValidate(), userHandler::listenValidateUser, UserOpenApi::validateUser)
                .build();
    }
}
