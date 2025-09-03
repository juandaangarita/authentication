package com.onix.model.users;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class User {
    private UUID userId;
    private String name;
    private String lastname;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String email;
    private String documentNumber;
    private Long baseSalary;
    private Integer roleId;
    private String roleName;
    private String password;
}
