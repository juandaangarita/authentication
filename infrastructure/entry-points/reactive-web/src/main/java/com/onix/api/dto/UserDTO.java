package com.onix.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;

public record UserDTO(
        UUID userId,
        String name,
        String lastname,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        String documentNumber,
        Long baseSalary
) {
}
