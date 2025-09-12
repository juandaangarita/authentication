package com.onix.api.mapper;

import com.onix.api.dto.LoginDTO;
import com.onix.api.dto.TokenDTO;
import com.onix.model.login.Login;
import com.onix.model.login.Token;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMapper {

    LoginDTO loginToDTO(Login login);
    TokenDTO tokenToDTO(Token token);

    Login loginToModel(LoginDTO dto);
    Token tokenToModel(TokenDTO dto);
}
