package cbtis239.model;

public class Docente {

    private int docenteId;
    private String curp;
    private String correo;
    private String nss;
    private String nombre;
    private String paterno;
    private String materno;
    private String telefono;
    private String celular;
    private int edoCivilId;
    private int generoId;

    // ===== Getters / Setters =====
    public int getDocenteId() { return docenteId; }
    public void setDocenteId(int docenteId) { this.docenteId = docenteId; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNss() { return nss; }
    public void setNss(String nss) { this.nss = nss; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPaterno() { return paterno; }
    public void setPaterno(String paterno) { this.paterno = paterno; }

    public String getMaterno() { return materno; }
    public void setMaterno(String materno) { this.materno = materno; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public int getEdoCivilId() { return edoCivilId; }
    public void setEdoCivilId(int edoCivilId) { this.edoCivilId = edoCivilId; }

    public int getGeneroId() { return generoId; }
    public void setGeneroId(int generoId) { this.generoId = generoId; }
}
