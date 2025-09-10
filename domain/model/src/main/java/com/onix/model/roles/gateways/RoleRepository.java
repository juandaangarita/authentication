package com.onix.model.roles.gateways;

import reactor.core.publisher.Mono;

public interface RoleRepository {
    Mono<String> findNameByRoleId(Integer roleId);
}
