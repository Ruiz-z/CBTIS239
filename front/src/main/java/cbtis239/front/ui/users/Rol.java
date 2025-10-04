package cbtis239.front.ui.users;

import javafx.beans.property.*;

public class Rol {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty permisos = new SimpleStringProperty();

    public Rol(int id, String nombre, String permisos) {
        setId(id);
        setNombre(nombre);
        setPermisos(permisos);
    }

    // --- ID ---
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // --- Nombre ---
    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public StringProperty nombreProperty() { return nombre; }

    // --- Permisos ---
    public String getPermisos() { return permisos.get(); }
    public void setPermisos(String permisos) { this.permisos.set(permisos); }
    public StringProperty permisosProperty() { return permisos; }
}
