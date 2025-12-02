package cbtis239.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Fila de la boleta de calificaciones de un alumno.
 * Curso = "Materia con Nombre del docente"
 */
public class BoletaCalificacion {

    private StringProperty curso = new SimpleStringProperty();
    private DoubleProperty parcial1 = new SimpleDoubleProperty();
    private DoubleProperty parcial2 = new SimpleDoubleProperty();
    private DoubleProperty parcial3 = new SimpleDoubleProperty();
    private DoubleProperty examenFinal = new SimpleDoubleProperty();

    // ========= Getters / setters =========
    public String getCurso() { return curso.get(); }
    public void setCurso(String value) { curso.set(value); }
    public StringProperty cursoProperty() { return curso; }

    public double getParcial1() { return parcial1.get(); }
    public void setParcial1(double value) { parcial1.set(value); }
    public DoubleProperty parcial1Property() { return parcial1; }

    public double getParcial2() { return parcial2.get(); }
    public void setParcial2(double value) { parcial2.set(value); }
    public DoubleProperty parcial2Property() { return parcial2; }

    public double getParcial3() { return parcial3.get(); }
    public void setParcial3(double value) { parcial3.set(value); }
    public DoubleProperty parcial3Property() { return parcial3; }

    public double getExamenFinal() { return examenFinal.get(); }
    public void setExamenFinal(double value) { examenFinal.set(value); }
    public DoubleProperty examenFinalProperty() { return examenFinal; }
}
