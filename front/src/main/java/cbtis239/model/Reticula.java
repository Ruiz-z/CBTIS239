package cbtis239.model;

import javafx.beans.property.*;

public class Reticula {
    private final IntegerProperty especialidadClave = new SimpleIntegerProperty();
    private final StringProperty  especialidadNombre = new SimpleStringProperty();
    private final StringProperty  materiaClave = new SimpleStringProperty();
    private final StringProperty  materiaNombre = new SimpleStringProperty();
    private final IntegerProperty semestre = new SimpleIntegerProperty();

    public int getEspecialidadClave() { return especialidadClave.get(); }
    public void setEspecialidadClave(int v) { especialidadClave.set(v); }
    public IntegerProperty especialidadClaveProperty() { return especialidadClave; }

    public String getEspecialidadNombre() { return especialidadNombre.get(); }
    public void setEspecialidadNombre(String v) { especialidadNombre.set(v); }
    public StringProperty especialidadNombreProperty() { return especialidadNombre; }

    public String getMateriaClave() { return materiaClave.get(); }
    public void setMateriaClave(String v) { materiaClave.set(v); }
    public StringProperty materiaClaveProperty() { return materiaClave; }

    public String getMateriaNombre() { return materiaNombre.get(); }
    public void setMateriaNombre(String v) { materiaNombre.set(v); }
    public StringProperty materiaNombreProperty() { return materiaNombre; }

    public int getSemestre() { return semestre.get(); }
    public void setSemestre(int v) { semestre.set(v); }
    public IntegerProperty semestreProperty() { return semestre; }
}
