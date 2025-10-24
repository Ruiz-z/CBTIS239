package cbtis239.model;

import javafx.beans.property.*;

public class DocenteMateria {
    private final IntegerProperty docenteId = new SimpleIntegerProperty(this, "docenteId");
    private final StringProperty docenteNombre = new SimpleStringProperty(this, "docenteNombre");
    private final StringProperty materiaClave = new SimpleStringProperty(this, "materiaClave");
    private final StringProperty materiaNombre = new SimpleStringProperty(this, "materiaNombre");

    public DocenteMateria() {}

    public DocenteMateria(int docenteId, String docenteNombre, String materiaClave, String materiaNombre) {
        setDocenteId(docenteId);
        setDocenteNombre(docenteNombre);
        setMateriaClave(materiaClave);
        setMateriaNombre(materiaNombre);
    }

    public int getDocenteId() { return docenteId.get(); }
    public void setDocenteId(int v) { docenteId.set(v); }
    public IntegerProperty docenteIdProperty() { return docenteId; }

    public String getDocenteNombre() { return docenteNombre.get(); }
    public void setDocenteNombre(String v) { docenteNombre.set(v); }
    public StringProperty docenteNombreProperty() { return docenteNombre; }

    public String getMateriaClave() { return materiaClave.get(); }
    public void setMateriaClave(String v) { materiaClave.set(v); }
    public StringProperty materiaClaveProperty() { return materiaClave; }

    public String getMateriaNombre() { return materiaNombre.get(); }
    public void setMateriaNombre(String v) { materiaNombre.set(v); }
    public StringProperty materiaNombreProperty() { return materiaNombre; }
}
