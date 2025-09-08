package com.onix.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String lastname,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        String documentNumber,
        Long baseSalary,
        String password,
        Integer roleId){
}
