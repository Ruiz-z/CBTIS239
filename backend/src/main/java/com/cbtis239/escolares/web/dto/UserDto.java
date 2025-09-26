package com.cbtis239.escolares.web.dto;

import java.util.Set;

public record UserDto(
        Long id,
        String fullName,
        String email,
        Set<String> roles,
        Boolean active
) {}
