package com.cbtis239.escolares.web.dto;

import java.util.Set;

public record UserUpdateRequest(
        String fullName,
        Boolean active,
        Set<String> roles
) {}
