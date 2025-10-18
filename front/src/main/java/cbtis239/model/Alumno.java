package cbtis239.model;

import java.time.LocalDate;

public class Alumno {

    // ====== Clave primaria y CURP ======
    private String matricula;
    private String curp;

    // ====== Datos personales ======
    private String nombre;
    private String paterno;
    private String materno;
    private String correo;
    private String nss;

    // ====== Catálogos / académicos ======
    private String estadoInscripcion;   // "Activo", "Inactivo", "Egresado"
    private Integer semestre;           // 1..6
    private Integer periodoId;          // FK Periodo
    private Integer edoCivilId;         // FK EdoCivil
    private Integer generoId;           // FK Genero
    private Integer grupoId;            // FK Grupo
    private String carrera;             // nombre de especialidad

    // ====== Dirección / contacto ======
    private String calle;
    private String numero;
    private String colonia;
    private String estado;              // <-- nuevo campo
    private String municipio;
    private String localidad;
    private String telefono;            // <-- nuevo campo
    private String celPadre;
    private String celMadre;

    // ====== Archivos ======
    private String foto;
    private String firma;

    // ====== Fecha ======
    private LocalDate fechaInscripcion;

    // ====== Getters / Setters ======

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPaterno() { return paterno; }
    public void setPaterno(String paterno) { this.paterno = paterno; }

    public String getMaterno() { return materno; }
    public void setMaterno(String materno) { this.materno = materno; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNss() { return nss; }
    public void setNss(String nss) { this.nss = nss; }

    public String getEstadoInscripcion() { return estadoInscripcion; }
    public void setEstadoInscripcion(String estadoInscripcion) { this.estadoInscripcion = estadoInscripcion; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }

    public Integer getPeriodoId() { return periodoId; }
    public void setPeriodoId(Integer periodoId) { this.periodoId = periodoId; }

    public Integer getEdoCivilId() { return edoCivilId; }
    public void setEdoCivilId(Integer edoCivilId) { this.edoCivilId = edoCivilId; }

    public Integer getGeneroId() { return generoId; }
    public void setGeneroId(Integer generoId) { this.generoId = generoId; }

    public Integer getGrupoId() { return grupoId; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCelPadre() { return celPadre; }
    public void setCelPadre(String celPadre) { this.celPadre = celPadre; }

    public String getCelMadre() { return celMadre; }
    public void setCelMadre(String celMadre) { this.celMadre = celMadre; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFirma() { return firma; }
    public void setFirma(String firma) { this.firma = firma; }

    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
}
