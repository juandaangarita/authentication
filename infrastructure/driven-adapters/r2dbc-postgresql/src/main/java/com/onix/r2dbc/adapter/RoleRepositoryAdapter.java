package com.onix.r2dbc.adapter;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
import com.onix.model.roles.Role;
import com.onix.model.roles.gateways.RoleRepository;
import com.onix.model.users.User;
import com.onix.model.users.exception.InvalidCredentialsException;
import com.onix.model.users.gateways.UserRepository;
import com.onix.r2dbc.entity.RoleEntity;
import com.onix.r2dbc.entity.UserEntity;
import com.onix.r2dbc.helper.ReactiveAdapterOperations;
import com.onix.r2dbc.repository.RoleReactiveRepository;
import com.onix.r2dbc.repository.UserReactiveRepository;
import com.onix.security.jwt.JwtProvider;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RoleRepositoryAdapter extends ReactiveAdapterOperations<
        Role,
        RoleEntity,
        Integer,
        RoleReactiveRepository
> implements RoleRepository {

    public RoleRepositoryAdapter(RoleReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Role.class));
    }

    @Override
    public Mono<String> findNameByRoleId(Integer roleId) {
        return repository.findById(roleId)
                .map(RoleEntity::getName);
    }
}
