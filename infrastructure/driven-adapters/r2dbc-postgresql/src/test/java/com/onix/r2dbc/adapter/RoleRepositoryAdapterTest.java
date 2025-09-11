package com.onix.r2dbc.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.r2dbc.entity.RoleEntity;
import com.onix.r2dbc.repository.RoleReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private RoleReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    private RoleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RoleRepositoryAdapter(repository, mapper);
    }

    @Test
    void shouldFindRoleNameByIdWhenRoleExists() {
        // Arrange
        Integer roleId = 1;
        String roleName = "ADMIN";
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleId(roleId);
        roleEntity.setName(roleName);

        when(repository.findById(roleId)).thenReturn(Mono.just(roleEntity));

        // Act & Assert
        StepVerifier.create(adapter.findNameByRoleId(roleId))
                .expectNext(roleName)
                .verifyComplete();
        verify(repository).findById(roleId);
    }

    @Test
    void shouldReturnEmptyMonoWhenRoleDoesNotExist() {
        // Arrange
        Integer roleId = 2;

        when(repository.findById(roleId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.findNameByRoleId(roleId))
                .verifyComplete();
        verify(repository).findById(roleId);
    }
}