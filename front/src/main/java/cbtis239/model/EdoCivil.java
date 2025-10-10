package cbtis239.model;

public class EdoCivil {
    private int idEdoCivil;
    private String nombre;

    public EdoCivil(int idEdoCivil, String nombre) {
        this.idEdoCivil = idEdoCivil;
        this.nombre = nombre;
    }

    public int getIdEdoCivil() { return idEdoCivil; }
    public void setIdEdoCivil(int idEdoCivil) { this.idEdoCivil = idEdoCivil; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override public String toString() { return nombre; }
}
