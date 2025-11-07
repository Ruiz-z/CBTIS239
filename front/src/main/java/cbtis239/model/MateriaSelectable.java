package cbtis239.model;

import javafx.beans.property.*;

public class MateriaSelectable {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty  clave    = new SimpleStringProperty();
    private final StringProperty  nombre   = new SimpleStringProperty();

    public MateriaSelectable(String clave, String nombre) {
        this.clave.set(clave); this.nombre.set(nombre);
    }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getClave() { return clave.get(); }
    public StringProperty claveProperty() { return clave; }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }
}
