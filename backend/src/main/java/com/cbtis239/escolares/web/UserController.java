package com.cbtis239.escolares.web;

import com.cbtis239.escolares.service.UserService;
import com.cbtis239.escolares.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserDto>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/roles")
    public ResponseEntity<java.util.List<RoleDto>> listRoles() {
        return ResponseEntity.ok(service.listRoles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
