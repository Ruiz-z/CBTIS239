package cbtis239.session;

import cbtis239.model.Docente;
import cbtis239.model.Usuario;

public class SesionActual {

    private static Usuario usuarioActual;
    private static Docente docenteActual;

    public static void setUsuario(Usuario u) { usuarioActual = u; }
    public static Usuario getUsuario() { return usuarioActual; }

    public static void setDocente(Docente d) { docenteActual = d; }
    public static Docente getDocente() { return docenteActual; }

    public static void limpiar() {
        usuarioActual = null;
        docenteActual = null;
    }
}
