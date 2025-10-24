package cbtis239.model;

public class Opcion {
    private final int id;
    private final String nombre;

    public Opcion(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override public String toString() { return nombre; } // para que el Combo muestre el nombre
}
