package cbtis239.dao;

import cbtis239.model.Aspirante;
import cbtis239.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AspiranteDAO {

    // ======================= EXISTE =======================
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

    // ======================= INSERT =======================
    public void insert(Aspirante a) throws SQLException {
        String sql = """
        INSERT INTO aspirante
        (CURP, FechaNacimiento, NSS, Folio,
         Telefono, Correo, TipoSangre,
         Altura, Peso, Estado, Municipio, Localidad,
         CalificacionExamenIngreso,
         OpcionEspecialidad1, OpcionEspecialidad2, OpcionEspecialidad3, OpcionEspecialidad4,
         EstatusPago, EstatusInscripcion, FechaRegistro,
         Nombre, Paterno, Materno,
         EdoCivil_idEdoCivil, Generos_idGenero,
         Calle, Numero, Colonia,
         CelPadre, CelMadre, CelAspirante,
         ContactoEmergencia, CorreoAspirante,
         Tutor1, Tutor2,
         Secundaria, EstadoSec, MunicipioSec,
         PromedioFinal, NombreSEC)
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            fillPS(ps, a, true);
            ps.executeUpdate();
        }
    }

    // ======================= UPDATE =======================
    public void update(Aspirante a) throws SQLException {
        String sql = """
        UPDATE aspirante SET
          CURP=?, FechaNacimiento=?, NSS=?,
          Telefono=?, Correo=?, TipoSangre=?,
          Altura=?, Peso=?, Estado=?, Municipio=?, Localidad=?,
          CalificacionExamenIngreso=?,
          OpcionEspecialidad1=?, OpcionEspecialidad2=?, OpcionEspecialidad3=?, OpcionEspecialidad4=?,
          EstatusPago=?, EstatusInscripcion=?, FechaRegistro=?,
          Nombre=?, Paterno=?, Materno=?,
          EdoCivil_idEdoCivil=?, Generos_idGenero=?,
          Calle=?, Numero=?, Colonia=?,
          CelPadre=?, CelMadre=?, CelAspirante=?,
          ContactoEmergencia=?, CorreoAspirante=?,
          Tutor1=?, Tutor2=?,
          Secundaria=?, EstadoSec=?, MunicipioSec=?,
          PromedioFinal=?, NombreSEC=?
        WHERE Folio=?
        """;

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = fillPS(ps, a, false);
            ps.setInt(i, a.getFolio());
            ps.executeUpdate();
        }
    }

    // ======================= fillPS =======================
    private int fillPS(PreparedStatement ps, Aspirante a, boolean incluirFolio) throws SQLException {
        int i = 1;

        ps.setString(i++, a.getCurp());
        ps.setObject(i++, a.getFechaNacimiento(), Types.DATE);
        ps.setString(i++, a.getNss());

        if (incluirFolio)
            ps.setInt(i++, a.getFolio());

        ps.setString(i++, a.getTelefono());
        ps.setString(i++, a.getCorreo());
        ps.setString(i++, a.getTipoSangre());

        ps.setObject(i++, a.getAltura(), Types.DECIMAL);
        ps.setObject(i++, a.getPeso(), Types.DECIMAL);

        ps.setString(i++, a.getEstado());
        ps.setString(i++, a.getMunicipio());
        ps.setString(i++, a.getLocalidad());

        ps.setObject(i++, a.getCalificacionExamenIngreso(), Types.DECIMAL);

        // ORDEN REAL EN BD
        ps.setObject(i++, a.getOpcionEspecialidad1(), Types.INTEGER);
        ps.setObject(i++, a.getOpcionEspecialidad2(), Types.INTEGER);
        ps.setObject(i++, a.getOpcionEspecialidad3(), Types.INTEGER);
        ps.setObject(i++, a.getOpcionEspecialidad4(), Types.INTEGER);

        ps.setString(i++, a.getEstatusPago());
        ps.setString(i++, a.getEstatusInscripcion());
        ps.setObject(i++, a.getFechaRegistro(), Types.DATE);

        ps.setString(i++, a.getNombre());
        ps.setString(i++, a.getPaterno());
        ps.setString(i++, a.getMaterno());

        ps.setObject(i++, a.getEdoCivilId(), Types.INTEGER);
        ps.setObject(i++, a.getGeneroId(), Types.INTEGER);

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

        ps.setObject(i++, a.getPromedioFinal(), Types.FLOAT);

        ps.setString(i++, a.getNombreSEC());

        return i;
    }

    // ======================= MAP =======================
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

        a.setAltura(rs.getObject("Altura") == null ? null : rs.getDouble("Altura"));
        a.setPeso(rs.getObject("Peso") == null ? null : rs.getDouble("Peso"));
        a.setCalificacionExamenIngreso(
                rs.getObject("CalificacionExamenIngreso") == null ? null : rs.getDouble("CalificacionExamenIngreso")
        );

        a.setOpcionEspecialidad1(getNullableInt(rs, "OpcionEspecialidad1"));
        a.setOpcionEspecialidad2(getNullableInt(rs, "OpcionEspecialidad2"));
        a.setOpcionEspecialidad3(getNullableInt(rs, "OpcionEspecialidad3"));
        a.setOpcionEspecialidad4(getNullableInt(rs, "OpcionEspecialidad4"));

        a.setEstatusPago(rs.getString("EstatusPago"));
        a.setEstatusInscripcion(rs.getString("EstatusInscripcion"));

        Date fr = rs.getDate("FechaRegistro");
        a.setFechaRegistro(fr == null ? null : fr.toLocalDate());

        a.setNombre(rs.getString("Nombre"));
        a.setPaterno(rs.getString("Paterno"));
        a.setMaterno(rs.getString("Materno"));

        a.setEdoCivilId(getNullableInt(rs, "EdoCivil_idEdoCivil"));
        a.setGeneroId(getNullableInt(rs, "Generos_idGenero"));

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

        // ⭐ AQUI ESTABA EL PROBLEMA ⭐
        a.setEstado(rs.getString("Estado"));
        a.setMunicipio(rs.getString("Municipio"));
        a.setLocalidad(rs.getString("Localidad"));

        a.setPromedioFinal(
                rs.getObject("PromedioFinal") == null ? null : rs.getFloat("PromedioFinal")
        );

        a.setNombreSEC(rs.getString("NombreSEC"));

        return a;
    }

    private Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    // ======================= DELETE =======================
    public int deleteByFolio(Connection cn, int folio) throws SQLException {
        String sql = "DELETE FROM aspirante WHERE Folio=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, folio);
            return ps.executeUpdate();
        }
    }

    public int deleteByFolio(int folio) throws SQLException {
        try (Connection cn = DB.get()) {
            return deleteByFolio(cn, folio);
        }
    }
    public void actualizarEstatusInscripcion(int folio, String nuevoEstatus) throws SQLException {
        try (Connection cn = DB.get();
             PreparedStatement ps =
                     cn.prepareStatement("UPDATE aspirante SET EstatusInscripcion=? WHERE Folio=?")) {

            ps.setString(1, nuevoEstatus);
            ps.setInt(2, folio);
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
        String sql = """
        SELECT Folio, CURP, Nombre, Paterno, Materno,
               EstatusInscripcion, EstatusPago,
               Telefono, Correo, Estado, Municipio, Localidad,
               NSS, Calle, Numero, Colonia,
               CelPadre, CelMadre, EdoCivil_idEdoCivil, Generos_idGenero
        FROM aspirante
        ORDER BY Folio DESC
        """;

        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Aspirante> out = new ArrayList<>();
            while (rs.next()) {

                Aspirante a = new Aspirante();
                a.setFolio(rs.getInt("Folio"));
                a.setCurp(rs.getString("CURP"));
                a.setNombre(rs.getString("Nombre"));
                a.setPaterno(rs.getString("Paterno"));
                a.setMaterno(rs.getString("Materno"));
                a.setEstatusInscripcion(rs.getString("EstatusInscripcion"));
                a.setEstatusPago(rs.getString("EstatusPago"));
                a.setTelefono(rs.getString("Telefono"));
                a.setCorreo(rs.getString("Correo"));
                a.setEstado(rs.getString("Estado"));
                a.setMunicipio(rs.getString("Municipio"));
                a.setLocalidad(rs.getString("Localidad"));
                a.setNss(rs.getString("NSS"));
                a.setCalle(rs.getString("Calle"));
                a.setNumero(rs.getString("Numero"));
                a.setColonia(rs.getString("Colonia"));
                a.setEstado(rs.getString("Estado"));
                a.setMunicipio(rs.getString("Municipio"));
                a.setLocalidad(rs.getString("Localidad"));
                a.setCelPadre(rs.getString("CelPadre"));
                a.setCelMadre(rs.getString("CelMadre"));
                a.setEdoCivilId(getNullableInt(rs, "EdoCivil_idEdoCivil"));
                a.setGeneroId(getNullableInt(rs, "Generos_idGenero"));

                out.add(a);
            }
            return out;
        }
    }

}
