package cbtis239.front.api.dto;

import java.util.Set;

public record UserCreateRequest(String fullName, String email, String password, Set<String> roles) {}
