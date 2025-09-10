package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class AuthenticateOpenApi {

    public void authenticateUser(Builder builder) {

        var requestExample = new LoginDTO("email@email.com", "password");

        var successResponse = new TokenDTO("sample-token");

        builder
                .operationId("loginUser")
                .summary("Authenticate a user")
                .description("Authenticates a user and returns a token")
                .tag("Login")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(LoginDTO.class))
                                .example(exampleOjectBuilder()
                                        .value(UtilOpenApi.createObjectToString(requestExample)))))
                .response(UtilOpenApi.responseApiBuilder(200, "Success", successResponse))
                .response(UtilOpenApi.responseApiBuilder(401, "Invalid Credentials", null))
                .response(UtilOpenApi.responseApiBuilder(500, "Internal server error", null))
        ;
    }
}
