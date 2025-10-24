package cbtis239.model;

import javafx.beans.property.*;

public class Docente {
    private final IntegerProperty docenteId = new SimpleIntegerProperty(this, "docenteId");
    private final StringProperty curp = new SimpleStringProperty(this, "curp");
    private final StringProperty correo = new SimpleStringProperty(this, "correo");
    private final StringProperty nss = new SimpleStringProperty(this, "nss");
    private final StringProperty nombre = new SimpleStringProperty(this, "nombre");
    private final StringProperty paterno = new SimpleStringProperty(this, "paterno");
    private final StringProperty materno = new SimpleStringProperty(this, "materno");
    private final StringProperty telefono = new SimpleStringProperty(this, "telefono");
    private final StringProperty celular = new SimpleStringProperty(this, "celular");
    private final IntegerProperty idEdoCivil = new SimpleIntegerProperty(this, "idEdoCivil");
    private final IntegerProperty idGenero = new SimpleIntegerProperty(this, "idGenero");

    // Para la tabla (texto del género y nombre completo)
    private final StringProperty generoNombre = new SimpleStringProperty(this, "generoNombre");

    public Docente() { }
    public Docente(int id, String curp, String correo, String nss, String nombre,
                   String paterno, String materno, String telefono, String celular,
                   int idEdoCivil, int idGenero, String generoNombre) {
        setDocenteId(id);
        setCurp(curp);
        setCorreo(correo);
        setNss(nss);
        setNombre(nombre);
        setPaterno(paterno);
        setMaterno(materno);
        setTelefono(telefono);
        setCelular(celular);
        setIdEdoCivil(idEdoCivil);
        setIdGenero(idGenero);
        setGeneroNombre(generoNombre);
    }

    public int getDocenteId() { return docenteId.get(); }
    public void setDocenteId(int v) { docenteId.set(v); }
    public IntegerProperty docenteIdProperty() { return docenteId; }

    public String getCurp() { return curp.get(); }
    public void setCurp(String v) { curp.set(v); }
    public StringProperty curpProperty() { return curp; }

    public String getCorreo() { return correo.get(); }
    public void setCorreo(String v) { correo.set(v); }
    public StringProperty correoProperty() { return correo; }

    public String getNss() { return nss.get(); }
    public void setNss(String v) { nss.set(v); }
    public StringProperty nssProperty() { return nss; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getPaterno() { return paterno.get(); }
    public void setPaterno(String v) { paterno.set(v); }
    public StringProperty paternoProperty() { return paterno; }

    public String getMaterno() { return materno.get(); }
    public void setMaterno(String v) { materno.set(v); }
    public StringProperty maternoProperty() { return materno; }

    public String getTelefono() { return telefono.get(); }
    public void setTelefono(String v) { telefono.set(v); }
    public StringProperty telefonoProperty() { return telefono; }

    public String getCelular() { return celular.get(); }
    public void setCelular(String v) { celular.set(v); }
    public StringProperty celularProperty() { return celular; }

    public int getIdEdoCivil() { return idEdoCivil.get(); }
    public void setIdEdoCivil(int v) { idEdoCivil.set(v); }
    public IntegerProperty idEdoCivilProperty() { return idEdoCivil; }

    public int getIdGenero() { return idGenero.get(); }
    public void setIdGenero(int v) { idGenero.set(v); }
    public IntegerProperty idGeneroProperty() { return idGenero; }

    public String getGeneroNombre() { return generoNombre.get(); }
    public void setGeneroNombre(String v) { generoNombre.set(v); }
    public StringProperty generoNombreProperty() { return generoNombre; }

    // Nombre completo para la tabla
    public String getNombreCompleto() {
        String p = getPaterno() == null ? "" : getPaterno();
        String m = getMaterno() == null ? "" : getMaterno();
        return (getNombre() + " " + p + " " + m).trim().replaceAll(" +", " ");
    }
}
