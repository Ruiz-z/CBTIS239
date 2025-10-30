package cbtis239.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Curso {
    private final IntegerProperty cursoId = new SimpleIntegerProperty(this, "cursoId");
    private final IntegerProperty anioAcademico = new SimpleIntegerProperty(this, "anioAcademico");
    private final StringProperty estado = new SimpleStringProperty(this, "estado");
    private final ObjectProperty<LocalDate> fechaInicio = new SimpleObjectProperty<>(this, "fechaInicio");
    private final ObjectProperty<LocalDate> fechaFin = new SimpleObjectProperty<>(this, "fechaFin");
    private final StringProperty descripcion = new SimpleStringProperty(this, "descripcion");

    private final StringProperty aulaId = new SimpleStringProperty(this, "aulaId");
    private final ObjectProperty<LocalTime> horaInicio = new SimpleObjectProperty<>(this, "horaInicio");
    private final ObjectProperty<LocalTime> horaFin = new SimpleObjectProperty<>(this, "horaFin");
    private final IntegerProperty diasSemanaId = new SimpleIntegerProperty(this, "diasSemanaId");

    private final IntegerProperty docenteId = new SimpleIntegerProperty(this, "docenteId");
    private final StringProperty materiaClave = new SimpleStringProperty(this, "materiaClave");

    // Para la tabla (nombres)
    private final StringProperty docenteNombre = new SimpleStringProperty(this, "docenteNombre");
    private final StringProperty materiaNombre = new SimpleStringProperty(this, "materiaNombre");
    private final StringProperty horarioTexto = new SimpleStringProperty(this, "horarioTexto");

    public int getCursoId() { return cursoId.get(); }
    public void setCursoId(int v) { cursoId.set(v); }
    public IntegerProperty cursoIdProperty() { return cursoId; }

    public int getAnioAcademico() { return anioAcademico.get(); }
    public void setAnioAcademico(int v) { anioAcademico.set(v); }
    public IntegerProperty anioAcademicoProperty() { return anioAcademico; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public StringProperty estadoProperty() { return estado; }

    public LocalDate getFechaInicio() { return fechaInicio.get(); }
    public void setFechaInicio(LocalDate v) { fechaInicio.set(v); }
    public ObjectProperty<LocalDate> fechaInicioProperty() { return fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin.get(); }
    public void setFechaFin(LocalDate v) { fechaFin.set(v); }
    public ObjectProperty<LocalDate> fechaFinProperty() { return fechaFin; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public StringProperty descripcionProperty() { return descripcion; }

    public String getAulaId() { return aulaId.get(); }
    public void setAulaId(String v) { aulaId.set(v); }
    public StringProperty aulaIdProperty() { return aulaId; }

    public LocalTime getHoraInicio() { return horaInicio.get(); }
    public void setHoraInicio(LocalTime v) { horaInicio.set(v); }
    public ObjectProperty<LocalTime> horaInicioProperty() { return horaInicio; }

    public LocalTime getHoraFin() { return horaFin.get(); }
    public void setHoraFin(LocalTime v) { horaFin.set(v); }
    public ObjectProperty<LocalTime> horaFinProperty() { return horaFin; }

    public int getDiasSemanaId() { return diasSemanaId.get(); }
    public void setDiasSemanaId(int v) { diasSemanaId.set(v); }
    public IntegerProperty diasSemanaIdProperty() { return diasSemanaId; }

    public int getDocenteId() { return docenteId.get(); }
    public void setDocenteId(int v) { docenteId.set(v); }
    public IntegerProperty docenteIdProperty() { return docenteId; }

    public String getMateriaClave() { return materiaClave.get(); }
    public void setMateriaClave(String v) { materiaClave.set(v); }
    public StringProperty materiaClaveProperty() { return materiaClave; }

    public String getDocenteNombre() { return docenteNombre.get(); }
    public void setDocenteNombre(String v) { docenteNombre.set(v); }
    public StringProperty docenteNombreProperty() { return docenteNombre; }

    public String getMateriaNombre() { return materiaNombre.get(); }
    public void setMateriaNombre(String v) { materiaNombre.set(v); }
    public StringProperty materiaNombreProperty() { return materiaNombre; }

    public String getHorarioTexto() { return horarioTexto.get(); }
    public void setHorarioTexto(String v) { horarioTexto.set(v); }
    public StringProperty horarioTextoProperty() { return horarioTexto; }
}
