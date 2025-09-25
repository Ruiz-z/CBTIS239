package com.cbtis239.escolares.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name; // ej: "USERS_READ", "USERS_CREATE"
}
