package cbtis239.model;

import javafx.beans.property.*;

public class Materia {
    private final StringProperty clave = new SimpleStringProperty(this, "clave");
    private final StringProperty nombre = new SimpleStringProperty(this, "nombre");
    private final IntegerProperty creditos = new SimpleIntegerProperty(this, "creditos");

    public Materia() {}
    public Materia(String clave, String nombre, Integer creditos) {
        setClave(clave);
        setNombre(nombre);
        setCreditos(creditos == null ? 0 : creditos);
    }

    public String getClave() { return clave.get(); }
    public void setClave(String value) { clave.set(value); }
    public StringProperty claveProperty() { return clave; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String value) { nombre.set(value); }
    public StringProperty nombreProperty() { return nombre; }

    public int getCreditos() { return creditos.get(); }
    public void setCreditos(int value) { creditos.set(value); }
    public IntegerProperty creditosProperty() { return creditos; }
}
