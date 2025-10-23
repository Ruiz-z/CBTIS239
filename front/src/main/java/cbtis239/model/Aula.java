package cbtis239.model;

import javafx.beans.property.*;

public class Aula {
    private final StringProperty clave = new SimpleStringProperty(this, "clave");
    private final IntegerProperty capacidad = new SimpleIntegerProperty(this, "capacidad");

    public Aula() {}
    public Aula(String clave, int capacidad) {
        setClave(clave);
        setCapacidad(capacidad);
    }

    public String getClave() { return clave.get(); }
    public void setClave(String value) { clave.set(value); }
    public StringProperty claveProperty() { return clave; }

    public int getCapacidad() { return capacidad.get(); }
    public void setCapacidad(int value) { capacidad.set(value); }
    public IntegerProperty capacidadProperty() { return capacidad; }
}
