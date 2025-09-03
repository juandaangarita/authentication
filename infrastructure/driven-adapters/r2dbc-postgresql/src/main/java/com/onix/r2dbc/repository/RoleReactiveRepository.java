package com.onix.r2dbc.repository;

import com.onix.r2dbc.entity.RoleEntity;
import com.onix.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RoleReactiveRepository extends ReactiveCrudRepository<RoleEntity, Integer>, ReactiveQueryByExampleExecutor<RoleEntity> {
}
