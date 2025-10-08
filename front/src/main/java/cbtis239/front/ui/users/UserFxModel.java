package cbtis239.front.ui.users;

import cbtis239.front.api.dto.UserDto;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UserFxModel {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty nombres = new SimpleStringProperty();
    private final StringProperty apellidoPat = new SimpleStringProperty();
    private final StringProperty apellidoMat = new SimpleStringProperty();
    private final StringProperty roles = new SimpleStringProperty();

    private Set<String> roleSet = new LinkedHashSet<>();

    public static UserFxModel fromDto(UserDto dto) {
        UserFxModel model = new UserFxModel();
        model.updateFromDto(dto);
        return model;
    }

    public void updateFromDto(UserDto dto) {
        if (dto.id() != null) {
            setId(dto.id());
        }
        setEmail(dto.email());
        setRoles(dto.roles() == null ? Set.of() : dto.roles());
        setFullName(dto.fullName());
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            setNombres("");
            setApellidoPat("");
            setApellidoMat("");
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            setNombres(parts[0]);
            setApellidoPat("");
            setApellidoMat("");
        } else if (parts.length == 2) {
            setNombres(parts[0]);
            setApellidoPat(parts[1]);
            setApellidoMat("");
        } else {
            setNombres(parts[0]);
            setApellidoPat(parts[1]);
            setApellidoMat(String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)));
        }
    }

    public LongProperty idProperty() {
        return id;
    }

    public long getId() {
        return id.get();
    }

    public void setId(long value) {
        this.id.set(value);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        this.email.set(value);
    }

    public StringProperty nombresProperty() {
        return nombres;
    }

    public String getNombres() {
        return nombres.get();
    }

    public void setNombres(String value) {
        this.nombres.set(value);
    }

    public StringProperty apellidoPatProperty() {
        return apellidoPat;
    }

    public String getApellidoPat() {
        return apellidoPat.get();
    }

    public void setApellidoPat(String value) {
        this.apellidoPat.set(value);
    }

    public StringProperty apellidoMatProperty() {
        return apellidoMat;
    }

    public String getApellidoMat() {
        return apellidoMat.get();
    }

    public void setApellidoMat(String value) {
        this.apellidoMat.set(value);
    }

    public StringProperty rolesProperty() {
        return roles;
    }

    public String getRolesText() {
        return roles.get();
    }

    public void setRoles(Set<String> roles) {
        java.util.List<String> normalized = (roles == null ? Set.<String>of() : roles).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .sorted(String::compareToIgnoreCase)
                .toList();
        this.roleSet = new LinkedHashSet<>(normalized);
        this.roles.set(String.join(", ", normalized));
    }

    public Set<String> getRoleSet() {
        return roleSet;
    }

    public String getPrimaryRole() {
        return roleSet.stream().findFirst().orElse(null);
    }

    public String getFullName() {
        return Arrays.asList(getNombres(), getApellidoPat(), getApellidoMat()).stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
