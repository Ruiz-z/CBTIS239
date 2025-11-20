package cbtis239.model;

import java.time.LocalDate;

public class AsistenciaDiaria {

    private int asistenciaID;
    private String alumnoMatricula;
    private LocalDate fecha;
    private String estadoAsistencia; // Presente / Falta / Justificada
    private String observaciones;

    public AsistenciaDiaria() {}

    public AsistenciaDiaria(int asistenciaID, String alumnoMatricula,
                            LocalDate fecha, String estadoAsistencia, String observaciones) {
        this.asistenciaID = asistenciaID;
        this.alumnoMatricula = alumnoMatricula;
        this.fecha = fecha;
        this.estadoAsistencia = estadoAsistencia;
        this.observaciones = observaciones;
    }

    public int getAsistenciaID() {
        return asistenciaID;
    }

    public void setAsistenciaID(int asistenciaID) {
        this.asistenciaID = asistenciaID;
    }

    public String getAlumnoMatricula() {
        return alumnoMatricula;
    }

    public void setAlumnoMatricula(String alumnoMatricula) {
        this.alumnoMatricula = alumnoMatricula;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getEstadoAsistencia() {
        return estadoAsistencia;
    }

    public void setEstadoAsistencia(String estadoAsistencia) {
        this.estadoAsistencia = estadoAsistencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
