package cbtis239.model;

public class AsistenciaBoletaResumen {

    private int diasEscolares;
    private int diasAsistidos;
    private double porcentaje;

    public AsistenciaBoletaResumen() {}

    public AsistenciaBoletaResumen(int diasEscolares, int diasAsistidos, double porcentaje) {
        this.diasEscolares = diasEscolares;
        this.diasAsistidos = diasAsistidos;
        this.porcentaje = porcentaje;
    }

    public int getDiasEscolares() {
        return diasEscolares;
    }

    public void setDiasEscolares(int diasEscolares) {
        this.diasEscolares = diasEscolares;
    }

    public int getDiasAsistidos() {
        return diasAsistidos;
    }

    public void setDiasAsistidos(int diasAsistidos) {
        this.diasAsistidos = diasAsistidos;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }
}
