package com.onix.r2dbc.helper;


import com.onix.r2dbc.entity.UserEntity;
import com.onix.security.config.JwtConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserDefaultGeneratorTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtConfigProperties jwtConfigProperties;
    @Mock
    private SqlIdentifier table;

    @InjectMocks
    private UserDefaultGenerator userDefaultGenerator;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));
        lenient().when(jwtConfigProperties.defaultPassword()).thenReturn("defaultPassword");
    }

    @Test
    void shouldGenerateDefaultValuesForNewUser() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setEmail("newuser@mail.com");

        // Act
        Publisher<UserEntity> resultPublisher = userDefaultGenerator.onBeforeConvert(entity, table);

        // Assert
        StepVerifier.create(resultPublisher)
                .assertNext(updatedEntity -> {
                    assertNotNull(updatedEntity.getUserId());
                    assertEquals("encoded_defaultPassword", updatedEntity.getPassword());
                    assertEquals(1, updatedEntity.getRoleId());
                })
                .verifyComplete();
    }

    @Test
    void shouldEncodePasswordAndKeepExistingId() {
        // Arrange
        UUID existingId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setUserId(existingId);
        entity.setEmail("existinguser@mail.com");
        entity.setPassword("userPassword123");

        // Act
        Publisher<UserEntity> resultPublisher = userDefaultGenerator.onBeforeConvert(entity, table);

        // Assert
        StepVerifier.create(resultPublisher)
                .assertNext(updatedEntity -> {
                    assertEquals(existingId, updatedEntity.getUserId());
                    assertEquals("encoded_userPassword123", updatedEntity.getPassword());
                    assertNotEquals("userPassword123", updatedEntity.getPassword());
                    // Assert that roleId is not changed if a value is provided, or a default is not set if already present
                    // This test case assumes roleId is null, and the default is applied.
                    assertEquals(1, updatedEntity.getRoleId());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleNullPasswordAndKeepExistingRoleId() {
        // Arrange
        UUID existingId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setUserId(existingId);
        entity.setEmail("anotheruser@mail.com");
        entity.setPassword(null); // Explicitly set to null
        entity.setRoleId(2); // A specific role id is already set

        // Act
        Publisher<UserEntity> resultPublisher = userDefaultGenerator.onBeforeConvert(entity, table);

        // Assert
        StepVerifier.create(resultPublisher)
                .assertNext(updatedEntity -> {
                    assertEquals(existingId, updatedEntity.getUserId());
                    assertEquals("encoded_defaultPassword", updatedEntity.getPassword());
                    assertEquals(2, updatedEntity.getRoleId()); // Role ID should not be changed
                })
                .verifyComplete();
    }
}