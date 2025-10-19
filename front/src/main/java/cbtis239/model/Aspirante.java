package cbtis239.model;

import java.time.LocalDate;

public class Aspirante {

    // ====== Clave primaria y CURP ======
    private Integer folio;
    private String curp;

    // ====== Datos personales ======
    private String nombre;
    private String paterno;
    private String materno;
    private LocalDate fechaNacimiento;
    private String nss;
    private String tipoSangre;
    private Double altura;
    private Double peso;
    private String correo;
    private String correoAspirante;
    private String telefono;
    private String celAspirante;
    private String contactoEmergencia;

    // ====== Catálogos / académicos ======
    private Float promedioFinal;
    private Double calificacionExamenIngreso;
    private String estatusPago;          // Pendiente / Pagado
    private String estatusInscripcion;   // Pendiente / Aceptado / Rechazado
    private Integer opcionEspecialidad1;
    private Integer opcionEspecialidad2;
    private Integer opcionEspecialidad3;
    private Integer opcionEspecialidad4;
    private Integer edoCivilId;          // FK EdoCivil
    private Integer generoId;            // FK Genero
    private LocalDate fechaRegistro;

    // ====== Dirección / contacto ======
    private String calle;
    private String numero;
    private String colonia;
    private String estado;
    private String municipio;
    private String localidad;

    // ====== Familiares / tutores ======
    private String celPadre;
    private String celMadre;
    private String tutor1;
    private String tutor2;

    // ====== Secundaria de procedencia ======
    private String secundaria;
    private String estadoSec;
    private String municipioSec;
    private String nombreSEC;

    // ====== Getters / Setters ======

    public Integer getFolio() { return folio; }
    public void setFolio(Integer folio) { this.folio = folio; }

    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPaterno() { return paterno; }
    public void setPaterno(String paterno) { this.paterno = paterno; }

    public String getMaterno() { return materno; }
    public void setMaterno(String materno) { this.materno = materno; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getNss() { return nss; }
    public void setNss(String nss) { this.nss = nss; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCorreoAspirante() { return correoAspirante; }
    public void setCorreoAspirante(String correoAspirante) { this.correoAspirante = correoAspirante; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCelAspirante() { return celAspirante; }
    public void setCelAspirante(String celAspirante) { this.celAspirante = celAspirante; }

    public String getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }

    public Float getPromedioFinal() { return promedioFinal; }
    public void setPromedioFinal(Float promedioFinal) { this.promedioFinal = promedioFinal; }

    public Double getCalificacionExamenIngreso() { return calificacionExamenIngreso; }
    public void setCalificacionExamenIngreso(Double calificacionExamenIngreso) { this.calificacionExamenIngreso = calificacionExamenIngreso; }

    public String getEstatusPago() { return estatusPago; }
    public void setEstatusPago(String estatusPago) { this.estatusPago = estatusPago; }

    public String getEstatusInscripcion() { return estatusInscripcion; }
    public void setEstatusInscripcion(String estatusInscripcion) { this.estatusInscripcion = estatusInscripcion; }

    public Integer getOpcionEspecialidad1() { return opcionEspecialidad1; }
    public void setOpcionEspecialidad1(Integer opcionEspecialidad1) { this.opcionEspecialidad1 = opcionEspecialidad1; }

    public Integer getOpcionEspecialidad2() { return opcionEspecialidad2; }
    public void setOpcionEspecialidad2(Integer opcionEspecialidad2) { this.opcionEspecialidad2 = opcionEspecialidad2; }

    public Integer getOpcionEspecialidad3() { return opcionEspecialidad3; }
    public void setOpcionEspecialidad3(Integer opcionEspecialidad3) { this.opcionEspecialidad3 = opcionEspecialidad3; }

    public Integer getOpcionEspecialidad4() { return opcionEspecialidad4; }
    public void setOpcionEspecialidad4(Integer opcionEspecialidad4) { this.opcionEspecialidad4 = opcionEspecialidad4; }

    public Integer getEdoCivilId() { return edoCivilId; }
    public void setEdoCivilId(Integer edoCivilId) { this.edoCivilId = edoCivilId; }

    public Integer getGeneroId() { return generoId; }
    public void setGeneroId(Integer generoId) { this.generoId = generoId; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

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

    public String getCelPadre() { return celPadre; }
    public void setCelPadre(String celPadre) { this.celPadre = celPadre; }

    public String getCelMadre() { return celMadre; }
    public void setCelMadre(String celMadre) { this.celMadre = celMadre; }

    public String getTutor1() { return tutor1; }
    public void setTutor1(String tutor1) { this.tutor1 = tutor1; }

    public String getTutor2() { return tutor2; }
    public void setTutor2(String tutor2) { this.tutor2 = tutor2; }

    public String getSecundaria() { return secundaria; }
    public void setSecundaria(String secundaria) { this.secundaria = secundaria; }

    public String getEstadoSec() { return estadoSec; }
    public void setEstadoSec(String estadoSec) { this.estadoSec = estadoSec; }

    public String getMunicipioSec() { return municipioSec; }
    public void setMunicipioSec(String municipioSec) { this.municipioSec = municipioSec; }

    public String getNombreSEC() { return nombreSEC; }
    public void setNombreSEC(String nombreSEC) { this.nombreSEC = nombreSEC; }
}
