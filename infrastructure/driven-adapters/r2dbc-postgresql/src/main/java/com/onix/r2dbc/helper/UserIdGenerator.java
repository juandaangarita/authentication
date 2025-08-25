package com.onix.r2dbc.helper;

import com.onix.r2dbc.entity.UserEntity;
import java.util.UUID;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserIdGenerator implements BeforeConvertCallback<UserEntity> {

    @Override
    public Publisher<UserEntity> onBeforeConvert(UserEntity entity, SqlIdentifier table) {
        if (entity.getUserId() == null) {
            entity.setUserId(UUID.randomUUID());
        }
        return Mono.just(entity);
    }
}
