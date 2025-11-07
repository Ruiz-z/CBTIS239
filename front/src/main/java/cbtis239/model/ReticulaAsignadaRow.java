package cbtis239.model;

import javafx.beans.property.*;

public class ReticulaAsignadaRow {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty  materiaClave  = new SimpleStringProperty();
    private final StringProperty  materiaNombre = new SimpleStringProperty();
    private final IntegerProperty semestre      = new SimpleIntegerProperty();

    public ReticulaAsignadaRow(String clave, String nombre, int semestre) {
        this.materiaClave.set(clave);
        this.materiaNombre.set(nombre);
        this.semestre.set(semestre);
    }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getMateriaClave() { return materiaClave.get(); }
    public StringProperty materiaClaveProperty() { return materiaClave; }

    public String getMateriaNombre() { return materiaNombre.get(); }
    public StringProperty materiaNombreProperty() { return materiaNombre; }

    public int getSemestre() { return semestre.get(); }
    public IntegerProperty semestreProperty() { return semestre; }
}
