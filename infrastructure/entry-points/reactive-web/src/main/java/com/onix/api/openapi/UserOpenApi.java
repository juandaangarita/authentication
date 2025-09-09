package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class UserOpenApi {

    public void createUser(Builder builder) {
        var successResponse = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                LocalDate.of(2000, 1, 1),
                "Street 123",
                "34567890",
                "email@email.com",
                "1234567890",
                15000L);

        var requestExample = new CreateUserDTO(
                "Pedro",
                "Perez",
                LocalDate.of(2000, 1, 1),
                "Street 123",
                "34567890",
                "email@email.com",
                "1234567890",
                15000L,
                "",
                null
                );

        builder
                .operationId("createUser")
                .summary("Create a new user")
                .description("Creates a user in the system")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CreateUserDTO.class))
                                .example(exampleOjectBuilder()
                                        .value(UtilOpenApi.createObjectToString(requestExample)))))
                .response(UtilOpenApi.responseApiBuilder(201, "User created successfully", successResponse))
                .response(UtilOpenApi.responseApiBuilder(400, "Validation error", null))
                .response(UtilOpenApi.responseApiBuilder(409, "Conflict error", null))
                .response(UtilOpenApi.responseApiBuilder(500, "Internal server error", null));
    }

    public void validateUser(Builder builder) {
        var successResponse = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                LocalDate.of(2000, 1, 1),
                "Street 123",
                "34567890",
                "email@email.com",
                "1234567890",
                15000L);

        builder
                .operationId("validateUser")
                .summary("Validate if a user exists")
                .description("Validates user by email and document number")
                .tag("User")
                .parameter(parameterBuilder()
                        .required(true)
                        .name("email")
                        .in(ParameterIn.QUERY)
                        .description("User email")
                        .example("email@email.com"))
                .parameter(parameterBuilder()
                        .required(true)
                        .name("documentNumber")
                        .in(ParameterIn.QUERY)
                        .description("User document number")
                        .example("1234567890"))
                .response(UtilOpenApi.responseApiBuilder(200, "User found", successResponse))
                .response(UtilOpenApi.responseApiBuilder(404, "User not found", null))
                .response(UtilOpenApi.responseApiBuilder(500, "Internal server error", null));
    }

    public void getUsersByEmails(Builder builder) {
        var successResponse = new UserDTO(
                UUID.randomUUID(),
                "Pedro",
                "Perez",
                LocalDate.of(2000, 1, 1),
                "Street 123",
                "34567890",
                "email@email.com",
                "1234567890",
                15000L);

        Set<String> requestExample = Set.of(
                "email@email.com",
                "email1@email.com",
                "email2@email.com");


        builder
                .operationId("getUsersByEmails")
                .summary("Batch get users")
                .description("Get users in the system by batch of emails")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(Set.class))
                                .example(exampleOjectBuilder()
                                        .value(UtilOpenApi.createObjectToString(requestExample)))))
                .response(UtilOpenApi.responseApiBuilder(201, "User created successfully", successResponse))
                .response(UtilOpenApi.responseApiBuilder(400, "Validation error", null))
                .response(UtilOpenApi.responseApiBuilder(409, "Conflict error", null))
                .response(UtilOpenApi.responseApiBuilder(500, "Internal server error", null));
    }
}
