package cbtis239.model;

public class Grupo {
    private int grupoId;
    private String nombreGrupo;
    private int capacidad;
    private int especialidadClave;
    // solo para mostrar en tabla:
    private String especialidadNombre;

    public Grupo(int grupoId, String nombreGrupo, int capacidad, int especialidadClave, String especialidadNombre) {
        this.grupoId = grupoId;
        this.nombreGrupo = nombreGrupo;
        this.capacidad = capacidad;
        this.especialidadClave = especialidadClave;
        this.especialidadNombre = especialidadNombre;
    }

    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public int getEspecialidadClave() { return especialidadClave; }
    public void setEspecialidadClave(int especialidadClave) { this.especialidadClave = especialidadClave; }

    public String getEspecialidadNombre() { return especialidadNombre; }
    public void setEspecialidadNombre(String especialidadNombre) { this.especialidadNombre = especialidadNombre; }
}
