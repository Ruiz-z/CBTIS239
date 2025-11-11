package cbtis239.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Calificacion {

    // ======= Propiedades base =======
    private IntegerProperty calificacionId = new SimpleIntegerProperty();
    private IntegerProperty cursoId = new SimpleIntegerProperty();
    private StringProperty alumnoMatricula = new SimpleStringProperty();
    private DoubleProperty parcial1 = new SimpleDoubleProperty();
    private DoubleProperty parcial2 = new SimpleDoubleProperty();
    private DoubleProperty parcial3 = new SimpleDoubleProperty();
    private DoubleProperty examenFinal = new SimpleDoubleProperty();
    private DoubleProperty promedio = new SimpleDoubleProperty();
    private ObjectProperty<LocalDateTime> fechaActualizacion = new SimpleObjectProperty<>();

    // ======= Relación con Alumno =======
    private ObjectProperty<Alumno> alumno = new SimpleObjectProperty<>();

    // ======= Getters / Setters =======

    public int getCalificacionId() { return calificacionId.get(); }
    public void setCalificacionId(int value) { this.calificacionId.set(value); }
    public IntegerProperty calificacionIdProperty() { return calificacionId; }

    public int getCursoId() { return cursoId.get(); }
    public void setCursoId(int value) { this.cursoId.set(value); }
    public IntegerProperty cursoIdProperty() { return cursoId; }

    public String getAlumnoMatricula() { return alumnoMatricula.get(); }
    public void setAlumnoMatricula(String value) { this.alumnoMatricula.set(value); }
    public StringProperty alumnoMatriculaProperty() { return alumnoMatricula; }

    public double getParcial1() { return parcial1.get(); }
    public void setParcial1(double value) { this.parcial1.set(value); }
    public DoubleProperty parcial1Property() { return parcial1; }

    public double getParcial2() { return parcial2.get(); }
    public void setParcial2(double value) { this.parcial2.set(value); }
    public DoubleProperty parcial2Property() { return parcial2; }

    public double getParcial3() { return parcial3.get(); }
    public void setParcial3(double value) { this.parcial3.set(value); }
    public DoubleProperty parcial3Property() { return parcial3; }

    public double getExamenFinal() { return examenFinal.get(); }
    public void setExamenFinal(double value) { this.examenFinal.set(value); }
    public DoubleProperty examenFinalProperty() { return examenFinal; }

    public double getPromedio() { return promedio.get(); }
    public void setPromedio(double value) { this.promedio.set(value); }
    public DoubleProperty promedioProperty() { return promedio; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion.get(); }
    public void setFechaActualizacion(LocalDateTime value) { this.fechaActualizacion.set(value); }
    public ObjectProperty<LocalDateTime> fechaActualizacionProperty() { return fechaActualizacion; }

    // ======= Asociación con Alumno =======
    public Alumno getAlumno() { return alumno.get(); }
    public void setAlumno(Alumno a) { this.alumno.set(a); }
    public ObjectProperty<Alumno> alumnoProperty() { return alumno; }

    // ======= Nombre completo derivado =======
    public String getNombreAlumnoCompleto() {
        if (alumno.get() != null) {
            return alumno.get().getNombreCompleto();
        }
        return "";
    }

    // ======= Cálculo automático de promedio =======
    public void recalcularPromedio() {
        double suma = 0;
        int count = 0;

        if (getParcial1() > 0) { suma += getParcial1(); count++; }
        if (getParcial2() > 0) { suma += getParcial2(); count++; }
        if (getParcial3() > 0) { suma += getParcial3(); count++; }
        if (getExamenFinal() > 0) { suma += getExamenFinal(); count++; }

        setPromedio(count > 0 ? suma / count : 0);
    }
}
