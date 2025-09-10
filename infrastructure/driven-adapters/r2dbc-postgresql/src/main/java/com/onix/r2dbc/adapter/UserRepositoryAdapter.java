package com.onix.r2dbc.adapter;

import com.onix.model.dto.LoginDTO;
import com.onix.model.dto.TokenDTO;
import com.onix.model.users.User;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.model.users.gateways.UserRepository;
import com.onix.r2dbc.entity.UserEntity;
import com.onix.r2dbc.helper.ReactiveAdapterOperations;
import com.onix.r2dbc.repository.UserReactiveRepository;

import com.onix.security.jwt.JwtProvider;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserReactiveRepository
> implements UserRepository {

    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepositoryAdapter roleRepository;

    public UserRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper, JwtProvider jwtProvider, PasswordEncoder passwordEncoder, RoleRepositoryAdapter roleRepository) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public Mono<User> saveUser(User user) {
        return super.save(user);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(entity -> mapper.map(entity, User.class));
    }

    @Override
    public Mono<TokenDTO> login(LoginDTO login) {
        return repository.findByEmail(login.email())
                .filter(userEntity -> passwordEncoder.matches(login.password(), userEntity.getPassword()))
                .flatMap(userEntity -> {
                    User user = mapper.map(userEntity, User.class);
                    return roleRepository.findNameByRoleId(user.getRoleId())
                            .map(roleName -> {
                                user.setRoleName(roleName);
                                return user;
                            });
                })
                .map(user -> new TokenDTO(jwtProvider.generateToken(login.email(), user.getRoleName())))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()));
    }
}
