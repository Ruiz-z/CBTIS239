package cbtis239.model;

public class Director {

    private Integer idDirector;   // siempre será 1
    private String nombre;
    private String paterno;
    private String materno;
    private byte[] firma;         // bytes de la imagen de la firma

    public Integer getIdDirector() {
        return idDirector;
    }

    public void setIdDirector(Integer idDirector) {
        this.idDirector = idDirector;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaterno() {
        return paterno;
    }

    public void setPaterno(String paterno) {
        this.paterno = paterno;
    }

    public String getMaterno() {
        return materno;
    }

    public void setMaterno(String materno) {
        this.materno = materno;
    }

    public byte[] getFirma() {
        return firma;
    }

    public void setFirma(byte[] firma) {
        this.firma = firma;
    }

    public String getNombreCompleto() {
        return nombre + " " + paterno + " " + materno;
    }
}
