package cbtis239.dao;

import cbtis239.model.Aspirante;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AspiranteDAO {

    // ====== CRUD ======

    public boolean existe(int folio) throws SQLException {
        String sql = "SELECT 1 FROM aspirante WHERE Folio=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // --- INSERT ---
    public void insert(Aspirante a) throws SQLException {
        String sql = """
        INSERT INTO aspirante
        (CURP, FechaNacimiento, NSS, Folio, Telefono, Correo, TipoSangre, Altura, Peso, Estado, Municipio, Localidad,
         CalificacionExamenIngreso, OpcionEspecialidad1, OpcionEspecialidad2, OpcionEspecialidad3, OpcionEspecialidad4,
         EstatusPago, EstatusInscripcion, FechaRegistro, Nombre, Paterno, Materno, EdoCivil_idEdoCivil, Generos_idGenero,
         Calle, Numero, Colonia, CelPadre, CelMadre, CelAspirante, ContactoEmergencia, CorreoAspirante, Tutor1, Tutor2,
         Secundaria, EstadoSec, MunicipioSec, PromedioFinal, NombreSEC)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fillPS(ps, a, true);  // ← true = incluir Folio
            ps.executeUpdate();
        }
    }

    // --- UPDATE ---
    public void update(Aspirante a) throws SQLException {
        String sql = """
        UPDATE aspirante SET
          CURP=?, FechaNacimiento=?, NSS=?, Telefono=?, Correo=?, TipoSangre=?, Altura=?, Peso=?, Estado=?, Municipio=?, Localidad=?,
          CalificacionExamenIngreso=?, OpcionEspecialidad1=?, OpcionEspecialidad2=?, OpcionEspecialidad3=?, OpcionEspecialidad4=?,
          EstatusPago=?, EstatusInscripcion=?, FechaRegistro=?, Nombre=?, Paterno=?, Materno=?, EdoCivil_idEdoCivil=?, Generos_idGenero=?,
          Calle=?, Numero=?, Colonia=?, CelPadre=?, CelMadre=?, CelAspirante=?, ContactoEmergencia=?, CorreoAspirante=?, Tutor1=?, Tutor2=?,
          Secundaria=?, EstadoSec=?, MunicipioSec=?, PromedioFinal=?, NombreSEC=?
        WHERE Folio=?
        """;
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = fillPS(ps, a, false); // ← false = NO incluir Folio
            ps.setInt(i, a.getFolio());   // último parámetro (WHERE)
            ps.executeUpdate();
        }
    }

    public void deleteByFolio(int folio) throws SQLException {
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM aspirante WHERE Folio=?")) {
            ps.setInt(1, folio);
            ps.executeUpdate();
        }
    }

    public Aspirante findByFolio(int folio) throws SQLException {
        String sql = "SELECT * FROM aspirante WHERE Folio=?";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Aspirante> listBreve() throws SQLException {
        String sql = "SELECT Folio, Nombre, Paterno, Materno, EstatusInscripcion FROM aspirante ORDER BY Folio DESC";
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Aspirante> out = new ArrayList<>();
            while (rs.next()) {
                Aspirante a = new Aspirante();
                a.setFolio(rs.getInt("Folio"));
                a.setNombre(rs.getString("Nombre"));
                a.setPaterno(rs.getString("Paterno"));
                a.setMaterno(rs.getString("Materno"));
                a.setEstatusInscripcion(rs.getString("EstatusInscripcion"));
                out.add(a);
            }
            return out;
        }
    }

    // ===== helpers =====

    private int fillPS(PreparedStatement ps, Aspirante a, boolean incluirFolio) throws SQLException {
        int i = 1;

        ps.setString(i++, a.getCurp());
        if (a.getFechaNacimiento() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(a.getFechaNacimiento()));
        ps.setString(i++, a.getNss());

        // ← Solo se incluye en INSERT
        if (incluirFolio) ps.setInt(i++, a.getFolio());

        ps.setString(i++, a.getTelefono());
        ps.setString(i++, a.getCorreo());
        ps.setString(i++, a.getTipoSangre());
        if (a.getAltura() == null) ps.setNull(i++, Types.DECIMAL);
        else ps.setDouble(i++, a.getAltura());
        if (a.getPeso() == null) ps.setNull(i++, Types.DECIMAL);
        else ps.setDouble(i++, a.getPeso());
        ps.setString(i++, a.getEstado());
        ps.setString(i++, a.getMunicipio());
        ps.setString(i++, a.getLocalidad());
        if (a.getCalificacionExamenIngreso() == null) ps.setNull(i++, Types.DECIMAL);
        else ps.setDouble(i++, a.getCalificacionExamenIngreso());
        if (a.getOpcionEspecialidad1() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getOpcionEspecialidad1());
        if (a.getOpcionEspecialidad2() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getOpcionEspecialidad2());
        if (a.getOpcionEspecialidad3() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getOpcionEspecialidad3());
        if (a.getOpcionEspecialidad4() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getOpcionEspecialidad4());
        ps.setString(i++, a.getEstatusPago());
        ps.setString(i++, a.getEstatusInscripcion());
        if (a.getFechaRegistro() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(a.getFechaRegistro()));
        ps.setString(i++, a.getNombre());
        ps.setString(i++, a.getPaterno());
        ps.setString(i++, a.getMaterno());
        if (a.getEdoCivilId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getEdoCivilId());
        if (a.getGeneroId() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, a.getGeneroId());
        ps.setString(i++, a.getCalle());
        ps.setString(i++, a.getNumero());
        ps.setString(i++, a.getColonia());
        ps.setString(i++, a.getCelPadre());
        ps.setString(i++, a.getCelMadre());
        ps.setString(i++, a.getCelAspirante());
        ps.setString(i++, a.getContactoEmergencia());
        ps.setString(i++, a.getCorreoAspirante());
        ps.setString(i++, a.getTutor1());
        ps.setString(i++, a.getTutor2());
        ps.setString(i++, a.getSecundaria());
        ps.setString(i++, a.getEstadoSec());
        ps.setString(i++, a.getMunicipioSec());
        if (a.getPromedioFinal() == null) ps.setNull(i++, Types.FLOAT);
        else ps.setFloat(i++, a.getPromedioFinal());
        ps.setString(i++, a.getNombreSEC());

        return i;
    }

    private Aspirante map(ResultSet rs) throws SQLException {
        Aspirante a = new Aspirante();

        a.setFolio(rs.getInt("Folio"));
        a.setCurp(rs.getString("CURP"));
        Date fn = rs.getDate("FechaNacimiento");
        a.setFechaNacimiento(fn == null ? null : fn.toLocalDate());
        a.setNss(rs.getString("NSS"));
        a.setTelefono(rs.getString("Telefono"));
        a.setCorreo(rs.getString("Correo"));
        a.setTipoSangre(rs.getString("TipoSangre"));
        a.setAltura(rs.getDouble("Altura"));
        a.setPeso(rs.getDouble("Peso"));
        a.setEstado(rs.getString("Estado"));
        a.setMunicipio(rs.getString("Municipio"));
        a.setLocalidad(rs.getString("Localidad"));
        a.setCalificacionExamenIngreso(rs.getDouble("CalificacionExamenIngreso"));
        a.setOpcionEspecialidad1(rs.getInt("OpcionEspecialidad1"));
        a.setOpcionEspecialidad2(rs.getInt("OpcionEspecialidad2"));
        a.setOpcionEspecialidad3(rs.getInt("OpcionEspecialidad3"));
        a.setOpcionEspecialidad4(rs.getInt("OpcionEspecialidad4"));
        a.setEstatusPago(rs.getString("EstatusPago"));
        a.setEstatusInscripcion(rs.getString("EstatusInscripcion"));
        Date fr = rs.getDate("FechaRegistro");
        a.setFechaRegistro(fr == null ? null : fr.toLocalDate());
        a.setNombre(rs.getString("Nombre"));
        a.setPaterno(rs.getString("Paterno"));
        a.setMaterno(rs.getString("Materno"));
        a.setEdoCivilId(rs.getInt("EdoCivil_idEdoCivil"));
        a.setGeneroId(rs.getInt("Generos_idGenero"));
        a.setCalle(rs.getString("Calle"));
        a.setNumero(rs.getString("Numero"));
        a.setColonia(rs.getString("Colonia"));
        a.setCelPadre(rs.getString("CelPadre"));
        a.setCelMadre(rs.getString("CelMadre"));
        a.setCelAspirante(rs.getString("CelAspirante"));
        a.setContactoEmergencia(rs.getString("ContactoEmergencia"));
        a.setCorreoAspirante(rs.getString("CorreoAspirante"));
        a.setTutor1(rs.getString("Tutor1"));
        a.setTutor2(rs.getString("Tutor2"));
        a.setSecundaria(rs.getString("Secundaria"));
        a.setEstadoSec(rs.getString("EstadoSec"));
        a.setMunicipioSec(rs.getString("MunicipioSec"));
        a.setPromedioFinal(rs.getFloat("PromedioFinal"));
        a.setNombreSEC(rs.getString("NombreSEC"));

        return a;
    }
}
