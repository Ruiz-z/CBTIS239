package cbtis239.model;

public class EdoCivil {
    private int idEdoCivil;
    private String nombre;

    // Nuevo: Constructor sin argumentos
    public EdoCivil() {
    }
    
    public EdoCivil(int idEdoCivil, String nombre) {
        this.idEdoCivil = idEdoCivil;
        this.nombre = nombre;
    }

    public int getIdEdoCivil() { return idEdoCivil; }
    public void setIdEdoCivil(int idEdoCivil) { this.idEdoCivil = idEdoCivil; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override public String toString() { return nombre; }
    
    // Es buena práctica incluir equals y hashCode basado en el ID para colecciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdoCivil edoCivil = (EdoCivil) o;
        return idEdoCivil == edoCivil.idEdoCivil;
    }

    @Override
    public int hashCode() {
        return idEdoCivil;
    }
}   