package cbtis239.model;

import javafx.beans.property.*;
import java.time.LocalTime;

/** Row para TableView con selección, máscara de días y horas. */
public class CursoRow {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty  materia = new SimpleStringProperty();
    private final StringProperty  docente = new SimpleStringProperty();
    private final StringProperty  horario = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final IntegerProperty diasMask = new SimpleIntegerProperty(0); // bits L..V
    private LocalTime hi;
    private LocalTime hf;
    private String aula;

    public CursoRow(int id, String materia, String docente, String horario,
                    int diasMask, LocalTime hi, LocalTime hf, String aula) {
        setId(id);
        setMateria(materia);
        setDocente(docente);
        setHorario(horario);
        setDiasMask(diasMask);
        this.hi = hi;
        this.hf = hf;
        this.aula = aula;
    }

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public IntegerProperty idProperty() { return id; }

    public String getMateria() { return materia.get(); }
    public void setMateria(String v) { materia.set(v); }
    public StringProperty materiaProperty() { return materia; }

    public String getDocente() { return docente.get(); }
    public void setDocente(String v) { docente.set(v); }
    public StringProperty docenteProperty() { return docente; }

    public String getHorario() { return horario.get(); }
    public void setHorario(String v) { horario.set(v); }
    public StringProperty horarioProperty() { return horario; }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }
    public BooleanProperty selectedProperty() { return selected; }

    public int getDiasMask() { return diasMask.get(); }
    public void setDiasMask(int m) { diasMask.set(m); }
    public IntegerProperty diasMaskProperty() { return diasMask; }

    public LocalTime getHi() { return hi; }
    public LocalTime getHf() { return hf; }
    public String getAula() { return aula; }
}
