package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateUserDTO;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class UserOpenApi {

    public void createUser(Builder builder) {
        var jsonContent = contentBuilder()
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ApiResponse.class));

        builder
                .operationId("createUser")
                .summary("Create a new user")
                .description("Creates a user in the system")
                .tag("User")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CreateUserDTO.class))))
                .response(responseBuilder()
                        .responseCode("201").description("User created successfully")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("400").description("Validation error")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("409").description("Conflict error")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("500").description("Internal server error")
                        .content(jsonContent));
    }

    public void validateUser(Builder builder) {
        var jsonContent = contentBuilder()
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ApiResponse.class));

        builder
                .operationId("validateUser")
                .summary("Validate if a user exists")
                .description("Validates user by email and document number")
                .tag("User")
                .parameter(parameterBuilder()
                        .required(true)
                        .name("email")
                        .in(ParameterIn.QUERY)
                        .description("User email"))
                .parameter(parameterBuilder()
                        .required(true)
                        .name("documentNumber")
                        .in(ParameterIn.QUERY)
                        .description("User document number"))
                .response(responseBuilder()
                        .responseCode("200").description("User found")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("404").description("User not found")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("500").description("Internal server error")
                        .content(jsonContent));
    }


}
