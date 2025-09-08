package com.onix.api.mapper;

import com.onix.api.dto.CreateUserDTO;
import com.onix.api.dto.UserDTO;
import com.onix.model.users.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    User toModel(CreateUserDTO dto);
}
