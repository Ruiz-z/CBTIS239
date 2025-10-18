package cbtis239.model;

import java.time.LocalDate;

public class Periodo {
    private int idPeriodo;
    private String nombre;
    private LocalDate inicio;
    private LocalDate fin;

    public Periodo() {}

    public Periodo(int idPeriodo, String nombre, LocalDate inicio, LocalDate fin) {
        this.idPeriodo = idPeriodo;
        this.nombre = nombre;
        this.inicio = inicio;
        this.fin = fin;
    }

    public int getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(int idPeriodo) { this.idPeriodo = idPeriodo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate inicio) { this.inicio = inicio; }

    public LocalDate getFin() { return fin; }
    public void setFin(LocalDate fin) { this.fin = fin; }

    @Override public String toString() { return nombre; }
}
