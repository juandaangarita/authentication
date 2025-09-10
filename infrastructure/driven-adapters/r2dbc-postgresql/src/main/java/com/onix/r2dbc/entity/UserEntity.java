package com.onix.r2dbc.entity;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("users")
public class UserEntity {

    @Id
    private UUID userId;
    private String name;
    private String lastname;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String email;
    private String documentNumber;
    private Long baseSalary;
    private String password;
    private Integer roleId;
}
