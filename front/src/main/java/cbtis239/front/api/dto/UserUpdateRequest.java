package cbtis239.front.api.dto;

import java.util.Set;

public record UserUpdateRequest(String fullName, Boolean active, Set<String> roles) {}
