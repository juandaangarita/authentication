package com.onix.r2dbc.helper;

import com.onix.r2dbc.entity.UserEntity;
import com.onix.security.config.JwtConfigProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDefaultGenerator implements BeforeConvertCallback<UserEntity> {

    private final PasswordEncoder passwordEncoder;
    private final JwtConfigProperties jwtConfigProperties;

    @Override
    public Publisher<UserEntity> onBeforeConvert(UserEntity entity, SqlIdentifier table) {
        log.debug("Generating user ID and encoding password for user: {}", entity);
        if (entity.getUserId() == null) {
            entity.setUserId(UUID.randomUUID());
        }
        if (entity.getPassword() == null) {
            entity.setPassword(passwordEncoder.encode(jwtConfigProperties.defaultPassword()));
        } else {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        if (entity.getRoleId() == null) {
            entity.setRoleId(1);
        }
        return Mono.just(entity);
    }
}
