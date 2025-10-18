package cbtis239.dao;

import cbtis239.model.Alumno;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    // === Ajusta tu URL/usuario/clave ===
    private Connection getConnection() throws SQLException {
        // Ejemplo MySQL
        // jdbc:mysql://host:puerto/bd?useSSL=false&serverTimezone=UTC
        String url = "jdbc:mysql://localhost:3306/SistemaEscolar?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String pass = "root";
        return DriverManager.getConnection(url, user, pass);
    }

    // ====== CRUD ======

    public boolean existe(String matricula) throws SQLException {
        String sql = "SELECT 1 FROM alumno WHERE Matricula=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Alumno a) throws SQLException {
        String sql = """
        INSERT INTO alumno
        (CURP, Matricula, GrupoID, Semestre, EstadoInscripcion, Foto, Firma, Telefono, Correo, FechaInscripcion,
         Nombre, Paterno, Materno, NSS, Carrera, Calle, Numero, Colonia, Estado, Municipio, Localidad,
         CelPadre, CelMadre, EdoCivil_idEdoCivil, Generos_idGenero, Periodo_idPeriodo)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fillPS(ps, a);
            ps.executeUpdate();
        }

    }

    public void update(Alumno a) throws SQLException {
        String sql = """
        UPDATE alumno SET
          CURP=?, Matricula=?, GrupoID=?, Semestre=?, EstadoInscripcion=?, Foto=?, Firma=?, Telefono=?, Correo=?, FechaInscripcion=?,
          Nombre=?, Paterno=?, Materno=?, NSS=?, Carrera=?, Calle=?, Numero=?, Colonia=?, Estado=?, Municipio=?, Localidad=?,
          CelPadre=?, CelMadre=?, EdoCivil_idEdoCivil=?, Generos_idGenero=?, Periodo_idPeriodo=?
        WHERE Matricula=?
    """;
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = fillPS(ps, a);
            ps.setString(i, a.getMatricula());
            ps.executeUpdate();
        }
    }


    public void deleteByMatricula(String matricula) throws SQLException {
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM alumno WHERE Matricula=?")) {
            ps.setString(1, matricula);
            ps.executeUpdate();
        }
    }

    public Alumno findByMatricula(String mat) throws SQLException {
        String sql = "SELECT * FROM alumno WHERE Matricula=?";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, mat);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Alumno> listBreve() throws SQLException {
        // Para llenar la tabla (matricula, nombre, semestre, grupo)
        String sql = "SELECT Matricula, Nombre, Semestre, GrupoID FROM alumno ORDER BY Matricula DESC LIMIT 100";
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Alumno> out = new ArrayList<>();
            while (rs.next()) {
                Alumno a = new Alumno();
                a.setMatricula(rs.getString("Matricula"));
                a.setNombre(rs.getString("Nombre"));
                int sem = rs.getInt("Semestre");
                a.setSemestre(rs.wasNull() ? null : sem);
                int gid = rs.getInt("GrupoID");
                a.setGrupoId(rs.wasNull() ? null : gid);
                out.add(a);
            }
            return out;
        }
    }

    // ===== helpers =====

    private int fillPS(PreparedStatement ps, Alumno a) throws SQLException {
        // Devuelve el siguiente índice libre (para UPDATE)
        int i = 1;

        ps.setString(i++, a.getCurp());
        ps.setString(i++, a.getMatricula());
        if (a.getGrupoId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getGrupoId());
        if (a.getSemestre() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getSemestre());
        ps.setString(i++, a.getEstadoInscripcion());
        ps.setString(i++, a.getFoto());
        ps.setString(i++, a.getFirma());
        ps.setString(i++, a.getTelefono());
        ps.setString(i++, a.getCorreo());
        if (a.getFechaInscripcion() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(a.getFechaInscripcion()));

        ps.setString(i++, a.getNombre());
        ps.setString(i++, a.getPaterno());
        ps.setString(i++, a.getMaterno());
        ps.setString(i++, a.getNss());
        ps.setString(i++, a.getCarrera());
        ps.setString(i++, a.getCalle());
        ps.setString(i++, a.getNumero());
        ps.setString(i++, a.getColonia());
        ps.setString(i++, a.getEstado());
        ps.setString(i++, a.getMunicipio());
        ps.setString(i++, a.getLocalidad());
        ps.setString(i++, a.getCelPadre());
        ps.setString(i++, a.getCelMadre());

        if (a.getEdoCivilId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getEdoCivilId());
        if (a.getGeneroId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getGeneroId());
        if (a.getPeriodoId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getPeriodoId());

        return i;
    }

    private Alumno map(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setMatricula(rs.getString("Matricula"));
        a.setCurp(rs.getString("CURP"));

        int gid = rs.getInt("GrupoID");  a.setGrupoId(rs.wasNull()? null : gid);
        int sem = rs.getInt("Semestre"); a.setSemestre(rs.wasNull()? null : sem);

        a.setEstadoInscripcion(rs.getString("EstadoInscripcion"));
        a.setFoto(rs.getString("Foto"));
        a.setFirma(rs.getString("Firma"));
        a.setCorreo(rs.getString("Correo"));

        Date d = rs.getDate("FechaInscripcion");
        a.setFechaInscripcion(d == null ? null : d.toLocalDate());

        a.setNombre(rs.getString("Nombre"));
        a.setPaterno(rs.getString("Paterno"));
        a.setMaterno(rs.getString("Materno"));
        a.setNss(rs.getString("NSS"));
        a.setCarrera(rs.getString("Carrera"));
        a.setCalle(rs.getString("Calle"));
        a.setNumero(rs.getString("Numero"));
        a.setColonia(rs.getString("Colonia"));
        a.setLocalidad(rs.getString("Localidad"));
        a.setMunicipio(rs.getString("Municipio"));
        a.setCelPadre(rs.getString("CelPadre"));
        a.setCelMadre(rs.getString("CelMadre"));

        int ec = rs.getInt("EdoCivil_idEdoCivil"); a.setEdoCivilId(rs.wasNull()? null : ec);
        int ge = rs.getInt("Generos_idGenero");    a.setGeneroId(rs.wasNull()? null : ge);
        int pe = rs.getInt("Periodo_idPeriodo");   a.setPeriodoId(rs.wasNull()? null : pe);

        return a;
    }
}

