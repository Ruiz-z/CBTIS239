package cbtis239.front.api.dto;

import java.util.Set;

public record UserDto(Long id, String fullName, String email, Set<String> roles, Boolean active) {}
