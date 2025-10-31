package cbtis239.bo;

import cbtis239.dao.AlumnoDAO;
import cbtis239.dao.PagoDAO;
import cbtis239.model.Pago;
import cbtis239.model.PagoHist;
import cbtis239.util.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PagoBO {

    public static final double MONTO_FIJO = 1200.00;
    private final PagoDAO dao = new PagoDAO();
    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    public String periodoActualNombre() throws SQLException {
        String n = dao.getPeriodoActualNombre();
        if (n == null)
            throw new SQLException("No hay un periodo vigente hoy.");
        return n;
    }

    public int periodoActualId() throws SQLException {
        Integer id = dao.getPeriodoActualId();
        if (id == null)
            throw new SQLException("No hay un periodo vigente hoy.");
        return id;
    }

    public ObservableList<Pago> listarTodos() {
        try {
            int periodo = periodoActualId();
            return FXCollections.observableArrayList(dao.listarTodosConEstado(periodo, MONTO_FIJO));
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<Pago> buscar(String entrada) {
        try {
            int periodo = periodoActualId();
            if (entrada == null || entrada.isBlank())
                return FXCollections.observableArrayList(dao.listarTodosConEstado(periodo, MONTO_FIJO));
            return FXCollections.observableArrayList(dao.buscarTodosConEstado(entrada.trim(), periodo, MONTO_FIJO));
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    // En PagoBO

    public Pago registrarPago(String entrada) throws SQLException {
        if (entrada == null || entrada.isBlank())
            throw new SQLException("Ingrese una matrícula o folio.");

        entrada = entrada.trim();
        Integer periodoId = dao.getPeriodoActualId();
        if (periodoId == null) throw new SQLException("No hay un periodo vigente hoy.");

        boolean esNumero = entrada.matches("\\d+");

        try (var cn = cbtis239.util.DB.get()) {
            cn.setAutoCommit(false);
            try {
                if (esNumero && dao.existsAspiranteFolio(Integer.parseInt(entrada))) {
                    int folio = Integer.parseInt(entrada);

                    // Intento directo: si ya hay pago, saltará SQLIntegrityConstraintViolationException
                    int id = dao.insertPagoAspiranteTx(cn, folio, MONTO_FIJO, periodoId);

                    // Si quieres, marca pagado al aspirante (opcional)
                    try (var ps = cn.prepareStatement("UPDATE sistemaescolar.aspirante SET EstatusPago='Pagado' WHERE Folio=?")) {
                        ps.setInt(1, folio);
                        ps.executeUpdate();
                    }

                    cn.commit();
                    String nombre = dao.nombreCompletoAspirante(folio);
                    return new Pago(id, 1, MONTO_FIJO, null, folio, periodoId, nombre);
                }

                // ALUMNO
                if (!dao.existsAlumnoMatricula(entrada))
                    throw new SQLException("La matrícula '" + entrada + "' no existe.");

                // Insertar pago (si hay duplicado, se captura abajo)
                int id = dao.insertPagoAlumnoTx(cn, entrada, MONTO_FIJO, periodoId);

                // Actualizar alumno tras pago (semestre/periodo/activo)
                new cbtis239.dao.AlumnoDAO().actualizarTrasPago(cn, entrada, periodoId);

                cn.commit();
                String nombre = dao.nombreCompletoAlumno(entrada);
                return new Pago(id, 1, MONTO_FIJO, entrada, null, periodoId, nombre);

            } catch (SQLException ex) {
                cn.rollback();
                // Traducimos DUPLICATE_* a mensajes de negocio
                if ("DUPLICATE_PAGO_ALUMNO".equals(ex.getMessage()))
                    throw new SQLException("Este alumno ya tiene un pago registrado en el periodo vigente.");
                if ("DUPLICATE_PAGO_ASPIRANTE".equals(ex.getMessage()))
                    throw new SQLException("Este aspirante ya tiene un pago registrado en el periodo vigente.");
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }


    // helper interno: insertar pago usando la misma conexión
    private int daoInsertPagoAlumnoTx(Connection cn, String matricula, double monto, int periodoId) throws SQLException {
        var sql = "INSERT INTO sistemaescolar.pago (Estatus, Monto, Alumno_Matricula, Aspirante_Folio, Periodo_idPeriodo) " +
                "VALUES (1, ?, ?, NULL, ?)";
        try (var ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, new java.math.BigDecimal(monto).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, matricula);
            ps.setInt(3, periodoId);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }
    public ObservableList<PagoHist> buscarHistorialTodosPeriodos(String entrada) throws SQLException {
        List<PagoHist> raw = dao.buscarHistorialTodosPeriodos(entrada);
        return FXCollections.observableArrayList(raw);
    }
}