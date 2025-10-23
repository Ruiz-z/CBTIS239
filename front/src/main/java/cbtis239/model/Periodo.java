package cbtis239.model;

import java.time.LocalDate;

public class Periodo {
    private int idPeriodo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Getters and Setters
    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    @Override
    public String toString() { return nombre; }
}
