package com.onix.api.dto;

import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String lastname,
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        String documentNumber,
        Long baseSalary) {
}
