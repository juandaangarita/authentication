package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.api.dto.ApiResponse;
import com.onix.model.dto.LoginDTO;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class AuthenticateOpenApi {

    public void authenticateUser(Builder builder) {
        var jsonContent = contentBuilder()
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ApiResponse.class));

        builder
                .operationId("loginUser")
                .summary("Authenticate a user")
                .description("Authenticates a user and returns a token")
                .tag("Login")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                .schema(schemaBuilder().implementation(LoginDTO.class))))
                .response(responseBuilder()
                        .responseCode("200").description("User found")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("400").description("Invalid Credentials")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("404").description("Email not found")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("500").description("Internal server error")
                        .content(jsonContent));
    }
}
