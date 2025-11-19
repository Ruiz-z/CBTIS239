package cbtis239.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Movimiento {

    private int movimientoID;
    private String alumnoMatricula;
    private LocalDate fecha;
    private LocalTime hora;
    private String tipoMovimiento; // Entrada / Salida

    public Movimiento() {}

    public Movimiento(int movimientoID, String alumnoMatricula,
                      LocalDate fecha, LocalTime hora, String tipoMovimiento) {
        this.movimientoID = movimientoID;
        this.alumnoMatricula = alumnoMatricula;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getMovimientoID() {
        return movimientoID;
    }

    public void setMovimientoID(int movimientoID) {
        this.movimientoID = movimientoID;
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

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
}
