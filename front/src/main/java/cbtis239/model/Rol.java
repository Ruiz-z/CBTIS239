package cbtis239.model;

public class Rol {
    private int idRol;         // RolID en BD
    private String nombre;     // NombreRol en BD
    private String descripcion;

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override public String toString() { return nombre; }
}
