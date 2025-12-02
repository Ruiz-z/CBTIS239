package cbtis239.model;

public class KardexAlumnoInfo {
    private String matricula;
    private String curp;
    private String nombreCompleto;
    private String carrera;
    private int especialidadClave;

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public int getEspecialidadClave() { return especialidadClave; }
    public void setEspecialidadClave(int especialidadClave) { this.especialidadClave = especialidadClave; }
}
