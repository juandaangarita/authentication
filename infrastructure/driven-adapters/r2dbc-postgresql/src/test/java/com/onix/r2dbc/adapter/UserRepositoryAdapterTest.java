package com.onix.r2dbc.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.dto.LoginDTO;
import com.onix.model.users.User;
import com.onix.r2dbc.entity.UserEntity;
import com.onix.r2dbc.repository.UserReactiveRepository;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.security.jwt.JwtProvider;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserReactiveRepository userReactiveRepository;
    @Mock
    private ObjectMapper mapper;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepositoryAdapter roleRepository;
    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private User user;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@mail.com")
                .password("password")
                .roleId(1)
                .build();
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID());
        userEntity.setEmail("test@mail.com");
        userEntity.setPassword("encodedPassword");
        userEntity.setRoleId(1);
    }

    @Test
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity);
        when(userReactiveRepository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.saveUser(user))
                .expectNext(user)
                .verifyComplete();

        verify(mapper).map(user, UserEntity.class);
        verify(userReactiveRepository).save(userEntity);
        verify(mapper).map(userEntity, User.class);
    }

    @Test
    void shouldFindByEmailWhenUserExists() {
        // Arrange
        when(userReactiveRepository.findByEmail("test@mail.com")).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.findByEmail("test@mail.com"))
                .expectNext(user)
                .verifyComplete();

        verify(userReactiveRepository).findByEmail("test@mail.com");
        verify(mapper).map(userEntity, User.class);
    }

    @Test
    void shouldReturnEmptyMonoWhenUserNotFoundByEmail() {
        // Arrange
        when(userReactiveRepository.findByEmail("test@mail.com")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.findByEmail("test@mail.com"))
                .expectNextCount(0)
                .verifyComplete();

        verify(userReactiveRepository).findByEmail("test@mail.com");
        verify(mapper, never()).map(any(), any());
    }

    @Test
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("test@mail.com", "password");
        when(userReactiveRepository.findByEmail("test@mail.com")).thenReturn(Mono.just(userEntity));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(mapper.map(userEntity, User.class)).thenReturn(user);
        when(roleRepository.findNameByRoleId(1)).thenReturn(Mono.just("USER"));
        when(jwtProvider.generateToken("test@mail.com", "USER")).thenReturn("mock-token");

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.login(loginDto))
                .expectNextMatches(tokenDto -> {
                    assertNotNull(tokenDto.token());
                    return "mock-token".equals(tokenDto.token());
                })
                .verifyComplete();

        verify(userReactiveRepository).findByEmail("test@mail.com");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(roleRepository).findNameByRoleId(1);
        verify(jwtProvider).generateToken("test@mail.com", "USER");
    }

    @Test
    void shouldFailLoginWhenPasswordIsIncorrect() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("test@mail.com", "wrong-password");
        when(userReactiveRepository.findByEmail("test@mail.com")).thenReturn(Mono.just(userEntity));
        when(passwordEncoder.matches("wrong-password", "encodedPassword")).thenReturn(false);

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.login(loginDto))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userReactiveRepository).findByEmail("test@mail.com");
        verify(passwordEncoder).matches("wrong-password", "encodedPassword");
        verify(roleRepository, never()).findNameByRoleId(any());
        verify(jwtProvider, never()).generateToken(any(), any());
    }

    @Test
    void shouldFailLoginWhenUserDoesNotExist() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("nonexistent@mail.com", "password");
        when(userReactiveRepository.findByEmail("nonexistent@mail.com")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(userRepositoryAdapter.login(loginDto))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userReactiveRepository).findByEmail("nonexistent@mail.com");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(roleRepository, never()).findNameByRoleId(any());
        verify(jwtProvider, never()).generateToken(any(), any());
    }
}