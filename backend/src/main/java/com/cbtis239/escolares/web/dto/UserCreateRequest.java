package com.cbtis239.escolares.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserCreateRequest(
        @NotBlank String fullName,
        @Email String email,
        @NotBlank String password,
        Set<String> roles // nombres de rol, p.ej. ["ADMIN","SERV_ESCOLARES"]
) {}
