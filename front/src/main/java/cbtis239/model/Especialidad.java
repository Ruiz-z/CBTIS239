package cbtis239.model;

public class Especialidad {
    private int clave;       // PK
    private String nombre;   // columna Nombre

    public Especialidad(int clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public int getClave() { return clave; }
    public void setClave(int clave) { this.clave = clave; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override public String toString() { return nombre; }
}
