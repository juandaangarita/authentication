package com.onix.api;

import com.onix.api.config.AuthenticationConfig;
import com.onix.model.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final AuthenticationConfig authConfig;
    private final UserHandler authUserHandler;

    @Bean
    @RouterOperation(
            path = "/api/v1/users",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE},
            beanClass = UserHandler.class,
            beanMethod = "listenSaveUser",
            operation = @Operation(
                    operationId = "createUser",
                    summary = "Create a new user",
                    description = "Creates a user in the system",
                    requestBody = @RequestBody(
                            required = true,
                            content = @Content(schema = @Schema(implementation = User.class))
                    ),
                    responses = {
                            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "409", description = "Conflict error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class)))
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(POST(authConfig.getUsers()), authUserHandler::listenSaveUser);
    }
}
