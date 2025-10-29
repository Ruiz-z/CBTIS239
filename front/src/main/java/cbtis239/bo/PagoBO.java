package cbtis239.bo;

import cbtis239.dao.PagoDAO;
import cbtis239.model.Pago;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.SQLException;

public class PagoBO {

    public static final double MONTO_FIJO = 1200.00;
    private final PagoDAO dao = new PagoDAO();

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

    public Pago registrarPago(String entrada) throws SQLException {
        if (entrada == null || entrada.isBlank())
            throw new SQLException("Ingrese una matrícula o folio.");

        entrada = entrada.trim();
        final int periodoId = periodoActualId();

        final boolean esNumero = entrada.matches("\\d+");
        if (esNumero) {
            final int folio = Integer.parseInt(entrada);

            if (dao.existsAspiranteFolio(folio)) {
                if (dao.existsPagoAspiranteEnPeriodo(folio, periodoId)) {
                    throw new SQLException("Este aspirante ya tiene un pago en el periodo vigente.");
                }
                int id = dao.insertPagoAspirante(folio, MONTO_FIJO, periodoId);
                dao.setAspiranteEstatusPagado(folio);
                String nombre = dao.nombreCompletoAspirante(folio);
                return new Pago(id, 1, MONTO_FIJO, null, folio, periodoId, nombre);
            }

            if (dao.existsAlumnoMatricula(entrada)) {
                if (dao.existsPagoAlumnoEnPeriodo(entrada, periodoId)) {
                    throw new SQLException("Se registro el pago correctamente");
                }
                int id = dao.insertPagoAlumno(entrada, MONTO_FIJO, periodoId);
                dao.setAlumnoEstadoActivo(entrada);
                String nombre = dao.nombreCompletoAlumno(entrada);
                return new Pago(id, 1, MONTO_FIJO, entrada, null, periodoId, nombre);
            }

            throw new SQLException("No se encontró ni aspirante con folio " + folio +
                    " ni alumno con matrícula '" + entrada + "'.");
        } else {
            if (!dao.existsAlumnoMatricula(entrada))
                throw new SQLException("La matrícula '" + entrada + "' no existe en alumno.");
            if (dao.existsPagoAlumnoEnPeriodo(entrada, periodoId)) {
                throw new SQLException("Registro de pago exitoso");
            }
            int id = dao.insertPagoAlumno(entrada, MONTO_FIJO, periodoId);
            dao.setAlumnoEstadoActivo(entrada);
            String nombre = dao.nombreCompletoAlumno(entrada);
            return new Pago(id, 1, MONTO_FIJO, entrada, null, periodoId, nombre);
        }
    }

}