package com.cbtis239.escolares.service;

import com.cbtis239.escolares.model.Role;
import com.cbtis239.escolares.model.User;
import com.cbtis239.escolares.repo.RoleRepository;
import com.cbtis239.escolares.repo.UserRepository;
import com.cbtis239.escolares.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDto create(UserCreateRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        Set<Role> roles = (req.roles() == null ? Set.<String>of() : req.roles())
                .stream()
                .map(name -> roleRepo.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no existe: " + name)))
                .collect(Collectors.toSet());

        User u = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .roles(roles)
                .active(true)
                .build();
        u = userRepo.save(u);
        return toDto(u);
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        return userRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public java.util.List<UserDto> list() {
        return userRepo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.active() != null) u.setActive(req.active());
        if (req.roles() != null) {
            Set<Role> roles = req.roles().stream()
                    .map(name -> roleRepo.findByName(name)
                            .orElseThrow(() -> new IllegalArgumentException("Rol no existe: " + name)))
                    .collect(Collectors.toSet());
            u.setRoles(roles);
        }
        return toDto(u);
    }

    @Transactional
    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                u.getActive()
        );
    }
}
